package client.messages.commands;

import client.MapleClient;
import constants.ServerConstants.CommandType;

public abstract class CommandExecute {
    public abstract int execute(MapleClient c, String[] splitted);

    enum ReturnValue {
        DONT_LOG,
        LOG;
    }

    public CommandType getType() {
        return CommandType.NORMAL;
    }

    public static abstract class TradeExecute extends CommandExecute {

        @Override
        public CommandType getType() {
            return CommandType.TRADE;
        }
    }
}
