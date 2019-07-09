package handling.channel.handler;

import client.BuddyList;
import client.BuddyList.BuddyAddResult;
import client.BuddyList.BuddyOperation;
import static client.BuddyList.BuddyOperation.ADDED;
import static client.BuddyList.BuddyOperation.DELETED;
import client.BuddylistEntry;
import client.CharacterNameAndId;
import client.MapleCharacter;
import client.MapleClient;
import database.DBConPool;
import handling.channel.ChannelServer;
import handling.world.World;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import tools.FileoutputUtil;
import tools.packet.MaplePacketCreator;
import tools.data.LittleEndianAccessor;

public class BuddyListHandler {

    private static final class CharacterIdNameBuddyCapacity extends CharacterNameAndId {

        private int buddyCapacity;

        public CharacterIdNameBuddyCapacity(int id, String name, String group, int buddyCapacity) {
            super(id, name, group);
            this.buddyCapacity = buddyCapacity;
        }

        public int getBuddyCapacity() {
            return buddyCapacity;
        }
    }

    private static final CharacterIdNameBuddyCapacity getCharacterIdAndNameFromDatabase(final String name, final String group) throws SQLException {
        CharacterIdNameBuddyCapacity ret = null;
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {

            PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE name LIKE ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt("gm") < 3) {
                    ret = new CharacterIdNameBuddyCapacity(rs.getInt("id"), rs.getString("name"), group, rs.getInt("buddyCapacity"));
                }
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError("logs/数据库异常.txt", ex);
        }
        return ret;
    }

    public static final void BuddyOperation(final LittleEndianAccessor slea, final MapleClient c) {
        final int mode = slea.readByte();
        final BuddyList buddylist = c.getPlayer().getBuddylist();

        if (mode == 1) { // add
            final String addName = slea.readMapleAsciiString();
            final String groupName = slea.readMapleAsciiString();
            final BuddylistEntry ble = buddylist.get(addName);

            if (addName.length() > 13 || groupName.length() > 16) {
                return;
            }
            if (ble != null && (ble.getGroup().equals(groupName) || !ble.isVisible())) {
                c.getSession().write(MaplePacketCreator.buddylistMessage((byte) 0x0B));
            } else if (ble != null && ble.isVisible()) {
                ble.setGroup(groupName);
                c.getSession().write(MaplePacketCreator.updateBuddylist(buddylist.getBuddies(), 0x07));
                c.getSession().write(MaplePacketCreator.buddylistMessage((byte) 0x0D));
            } else if (buddylist.isFull()) {
                c.getSession().write(MaplePacketCreator.buddylistMessage((byte) 0x0B));
            } else {
                try {
                    CharacterIdNameBuddyCapacity charWithId = null;
                    int channel = World.Find.findChannel(addName);
                    MapleCharacter otherChar = null;
                    if (channel > 0) {
                        otherChar = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(addName);
                        if (otherChar == null) {
                            charWithId = getCharacterIdAndNameFromDatabase(addName, groupName);
                        } else if (!otherChar.isIntern() || c.getPlayer().isIntern()) {
                            charWithId = new CharacterIdNameBuddyCapacity(otherChar.getId(), otherChar.getName(), groupName, otherChar.getBuddylist().getCapacity());
                        }
                    } else {
                        charWithId = getCharacterIdAndNameFromDatabase(addName, groupName);
                    }

                    if (charWithId != null) {
                        BuddyAddResult buddyAddResult = null;
                        if (channel > 0) {
                            buddyAddResult = World.Buddy.requestBuddyAdd(addName, c.getChannel(), c.getPlayer().getId(), c.getPlayer().getName(), c.getPlayer().getLevel(), c.getPlayer().getJob());
                        } else {
                            try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
                                PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) as buddyCount FROM buddies WHERE characterid = ? AND pending = 0");
                                ps.setInt(1, charWithId.getId());
                                ResultSet rs = ps.executeQuery();

                                if (!rs.next()) {
                                    ps.close();
                                    rs.close();
                                    throw new RuntimeException("Result set expected");
                                } else {
                                    int count = rs.getInt("buddyCount");
                                    if (count >= charWithId.getBuddyCapacity()) {
                                        buddyAddResult = BuddyAddResult.BUDDYLIST_FULL;
                                    }
                                }
                                rs.close();
                                ps.close();

                                ps = con.prepareStatement("SELECT pending FROM buddies WHERE characterid = ? AND buddyid = ?");
                                ps.setInt(1, charWithId.getId());
                                ps.setInt(2, c.getPlayer().getId());
                                rs = ps.executeQuery();
                                if (rs.next()) {
                                    buddyAddResult = BuddyAddResult.ALREADY_ON_LIST;
                                }
                                rs.close();
                                ps.close();
                            } catch (SQLException ex) {
                                FileoutputUtil.outputFileError("logs/数据库异常.txt", ex);
                            }
                        }
                        if (buddyAddResult == BuddyAddResult.BUDDYLIST_FULL) {
                            c.getSession().write(MaplePacketCreator.buddylistMessage((byte) 0x0C));
                        } else {
                            int displayChannel = -1;
                            int otherCid = charWithId.getId();
                            if (buddyAddResult == BuddyAddResult.ALREADY_ON_LIST && channel > 0) {
                                displayChannel = channel;
                                notifyRemoteChannel(c, channel, otherCid, groupName, ADDED);
                            } else if (buddyAddResult != BuddyAddResult.ALREADY_ON_LIST) {
                                try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
                                    PreparedStatement ps = con.prepareStatement("INSERT INTO buddies (`characterid`, `buddyid`, `groupname`, `pending`) VALUES (?, ?, ?, 1)");
                                    ps.setInt(1, charWithId.getId());
                                    ps.setInt(2, c.getPlayer().getId());
                                    ps.setString(3, groupName);
                                    ps.executeUpdate();
                                    ps.close();
                                } catch (SQLException exx) {
                                    FileoutputUtil.outputFileError("logs/数据库异常.txt", exx);
                                }
                            }
                            buddylist.put(new BuddylistEntry(charWithId.getName(), otherCid, groupName, displayChannel, true));
                            c.getSession().write(MaplePacketCreator.updateBuddylist(buddylist.getBuddies(), 7));
                        }
                    } else {
                        c.getSession().write(MaplePacketCreator.buddylistMessage((byte) 0x0F));
                    }
                } catch (SQLException exxx) {
                    System.err.println("SQL THROW" + exxx);
                    FileoutputUtil.outputFileError("logs/数据库异常.txt", exxx);
                }
            }
        } else if (mode == 2) { // accept buddy
            int otherCid = slea.readInt();
            final BuddylistEntry ble = buddylist.get(otherCid);
            if (!buddylist.isFull() && ble != null && !ble.isVisible()) {
                final int channel = World.Find.findChannel(otherCid);
                buddylist.put(new BuddylistEntry(ble.getName(), otherCid, "群未定", channel, true));
                c.getSession().write(MaplePacketCreator.updateBuddylist(buddylist.getBuddies(), 0x07));
                notifyRemoteChannel(c, channel, otherCid, "群未定", ADDED);
            } else {
                c.getSession().write(MaplePacketCreator.buddylistMessage((byte) 0x0B));
            }
        } else if (mode == 3) { // delete
            final int otherCid = slea.readInt();
            final BuddylistEntry blz = buddylist.get(otherCid);
            if (blz != null && blz.isVisible()) {
                notifyRemoteChannel(c, World.Find.findChannel(otherCid), otherCid, blz.getGroup(), DELETED);
            }
            buddylist.remove(otherCid);
            c.getSession().write(MaplePacketCreator.updateBuddylist(buddylist.getBuddies(), 0x07));
        }
    }

    private static final void notifyRemoteChannel(final MapleClient c, final int remoteChannel, final int otherCid, final String group, final BuddyOperation operation) {
        final MapleCharacter player = c.getPlayer();

        if (remoteChannel > 0) {
            World.Buddy.buddyChanged(otherCid, player.getId(), player.getName(), c.getChannel(), operation, group);
        }
    }
}
