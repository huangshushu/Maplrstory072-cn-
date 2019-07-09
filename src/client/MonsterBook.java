package client;

import constants.GameConstants;
import database.DBConPool;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import server.MapleItemInformationProvider;
import tools.FileoutputUtil;
import tools.packet.MaplePacketCreator;
import tools.data.MaplePacketLittleEndianWriter;

public class MonsterBook implements Serializable {

    private static final long serialVersionUID = 7179541993413738569L;
    private boolean changed = false;
    private int SpecialCard = 0, NormalCard = 0, BookLevel = 1;
    private Map<Integer, Integer> cards;

    public MonsterBook(Map<Integer, Integer> cards) {
        this.cards = cards;

        for (Entry<Integer, Integer> card : cards.entrySet()) {
            if (GameConstants.isSpecialCard(card.getKey())) {
                SpecialCard += card.getValue();
            } else {
                NormalCard += card.getValue();
            }
        }
        calculateLevel();
    }

    public Map<Integer, Integer> getCards() {
        return cards;
    }

    public final int getTotalCards() {
        return SpecialCard + NormalCard;
    }

    public final int getLevelByCard(final int cardid) {
        return cards.get(cardid) == null ? 0 : cards.get(cardid);
    }

    public final static MonsterBook loadCards(final int charid) throws SQLException {
        Map<Integer, Integer> cards;
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM monsterbook WHERE charid = ? ORDER BY cardid ASC")) {
            ps.setInt(1, charid);
            try (ResultSet rs = ps.executeQuery()) {
                cards = new LinkedHashMap<>();
                int cardid, level;
                while (rs.next()) {
                    cards.put(rs.getInt("cardid"), rs.getInt("level"));
                }
            }
        }
        return new MonsterBook(cards);
    }

    public final void saveCards(final int charid, Connection con) throws SQLException {
        if (!changed) {
            return;
        }
        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM monsterbook WHERE charid = ?");
            ps.setInt(1, charid);
            ps.execute();
            ps.close();
            changed = false;
            if (cards.isEmpty()) {
                return;
            }

            boolean first = true;
            final StringBuilder query = new StringBuilder();

            for (final Entry<Integer, Integer> all : cards.entrySet()) {
                if (first) {
                    first = false;
                    query.append("INSERT INTO monsterbook VALUES (DEFAULT,");
                } else {
                    query.append(",(DEFAULT,");
                }
                query.append(charid);
                query.append(",");
                query.append(all.getKey()); // Card ID
                query.append(",");
                query.append(all.getValue()); // Card level
                query.append(")");
            }
            ps = con.prepareStatement(query.toString());
            ps.execute();
            ps.close();
        } catch (Exception se) {
            FileoutputUtil.outputFileError("logs/数据库异常.txt", se);
        }
    }

    private final void calculateLevel() {
        int Size = NormalCard + SpecialCard;
        BookLevel = 8;

        for (int i = 0; i < 8; i++) {
            if (Size <= GameConstants.getBookLevel(i)) {
                BookLevel = (i + 1);
                break;
            }
        }
    }

    public final void addCardPacket(final MaplePacketLittleEndianWriter mplew) {
        mplew.writeShort(cards.size());
        for (Entry<Integer, Integer> all : cards.entrySet()) {
            mplew.writeShort(GameConstants.getCardShortId(all.getKey())); // Id
            mplew.write(all.getValue()); // Level
        }
    }

    public final void addCharInfoPacket(final int bookcover, final MaplePacketLittleEndianWriter mplew) {
        mplew.writeInt(BookLevel);
        mplew.writeInt(NormalCard);
        mplew.writeInt(SpecialCard);
        mplew.writeInt(NormalCard + SpecialCard);
        mplew.writeInt(MapleItemInformationProvider.getInstance().getCardMobId(bookcover));
    }

    public final void updateCard(final MapleClient c, final int cardid) {
        c.getSession().write(MaplePacketCreator.changeCardSet(cardid));
    }

    public final void addCard(final MapleClient c, final int cardid) {
        changed = true;
        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showForeignEffect(c.getPlayer().getId(), 0x0E), false);

        if (cards.containsKey(cardid)) {
            final int levels = cards.get(cardid);
            if (levels >= 5) {
                c.getSession().write(MaplePacketCreator.getCard(true, cardid, levels));
            } else {
                if (GameConstants.isSpecialCard(cardid)) {
                    SpecialCard += 1;
                } else {
                    NormalCard += 1;
                }
                c.getSession().write(MaplePacketCreator.getCard(false, cardid, levels + 1));
                c.getSession().write(MaplePacketCreator.showGainCard(cardid));
                c.getSession().write(MaplePacketCreator.showSpecialEffect(0x0E));
                cards.put(cardid, levels + 1);
                calculateLevel();
            }
            return;
        }
        if (GameConstants.isSpecialCard(cardid)) {
            SpecialCard += 1;
        } else {
            NormalCard += 1;
        }
        // New card
        cards.put(cardid, 1);
        c.getSession().write(MaplePacketCreator.getCard(false, cardid, 1));
        c.getSession().write(MaplePacketCreator.showGainCard(cardid));
        c.getSession().write(MaplePacketCreator.showSpecialEffect(0x0E));
        calculateLevel();
    }
}
