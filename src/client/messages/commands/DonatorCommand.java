package client.messages.commands;

import constants.ServerConstants.PlayerGMRank;

public class DonatorCommand {
    
    public static PlayerGMRank getPlayerLevelRequired() {
        return PlayerGMRank.DONATOR;
    }
}
