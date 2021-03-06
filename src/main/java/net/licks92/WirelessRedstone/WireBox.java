package net.licks92.WirelessRedstone;

import net.licks92.WirelessRedstone.Channel.*;
import net.licks92.WirelessRedstone.Channel.WirelessReceiver.Type;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class WireBox {
    private final WirelessRedstone plugin;

    public WireBox(final WirelessRedstone wirelessRedstone) {
        this.plugin = wirelessRedstone;
    }

    public HashMap<Integer, String> clockTasks = new HashMap<Integer, String>();
    public HashMap<Location, Boolean> switchState = new HashMap<Location, Boolean>();
    public ArrayList<String> activeChannels = new ArrayList<String>();

    public int signFaceToInt(final BlockFace face) {
        switch (face) {
            case NORTH:
                return 8;
            case EAST:
                return 12;
            case SOUTH:
                return 0;
            case WEST:
                return 4;
            default:
                return 0;
        }
    }

    public BlockFace intToBlockFaceSign(final int dir) {
        switch (dir) {
            case 8:
                return BlockFace.NORTH;
            case 12:
                return BlockFace.EAST;
            case 0:
                return BlockFace.SOUTH;
            case 4:
                return BlockFace.WEST;
            default:
                return BlockFace.NORTH;
        }
    }

    /**
     * @param data - The line of the sign
     * @return true if string corresponds to the tag of the transmitter.
     */
    public boolean isTransmitter(final String data) {
        for (String tag : WirelessRedstone.strings.tagsTransmitter) {
            if (data.toLowerCase().equals(tag.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param data - The line of the sign
     * @return true if string corresponds to the tag of the receiver.
     */
    public boolean isReceiver(final String data) {
        for (String tag : WirelessRedstone.strings.tagsReceiver) {
            if (data.toLowerCase().equals(tag.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param data - The line of the sign
     * @return true if string corresponds to the tag of the screen.
     */
    public boolean isScreen(final String data) {
        for (String tag : WirelessRedstone.strings.tagsScreen) {
            if (data.toLowerCase().equals(tag.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param data - The line of the sign
     * @return true if the string corresponds to the tag of the inverter
     * receiver type
     */
    public boolean isReceiverInverter(final String data) {
        for (String tag : WirelessRedstone.strings.tagsReceiverInverterType) {
            if (data.toLowerCase().equals(tag.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param data - The line of the sign
     * @return true if the string corresponds to the tag of the delayer receiver
     * type
     */
    public boolean isReceiverDelayer(final String data) {
        for (String tag : WirelessRedstone.strings.tagsReceiverDelayerType) {
            if (data.toLowerCase().equals(tag.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param data - The line of the sign
     * @return true if the string corresponds to the tag of the delayer receiver
     * type
     */
    public boolean isReceiverClock(final String data) {
        for (String tag : WirelessRedstone.strings.tagsReceiverClockType) {
            if (data.toLowerCase().equals(tag.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param data - The line of the sign
     * @return true if the string corresponds to the tag of the delayer receiver
     * type
     */
    public boolean isReceiverSwitch(final String data) {
        for (String tag : WirelessRedstone.strings.tagsReceiverSwitchType) {
            if (data.toLowerCase().equals(tag.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param data - The line of the sign
     * @return true if the string corresponds to the tag of the default receiver
     * type
     */
    public boolean isReceiverDefault(final String data) {
        for (String tag : WirelessRedstone.strings.tagsReceiverDefaultType) {
            if (data.toLowerCase().equals(tag.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAccessToChannel(final Player player,
                                      final String channelname) {
        if (WirelessRedstone.config.getWirelessChannel(channelname) != null) {
            return this.plugin.permissions.isWirelessAdmin(player)
                    || WirelessRedstone.config.getWirelessChannel(channelname).getOwners().contains(player.getName());
        }
        return true;
    }

    public boolean addWirelessReceiver(final String cname, final Block cblock,
                                       final Player player, final Type type) {
        org.bukkit.material.Sign sign = (org.bukkit.material.Sign) cblock
                .getState().getData();
        WirelessRedstone.getWRLogger().debug(
                "Adding a receiver at location "
                        + cblock.getLocation().getBlockX() + ","
                        + cblock.getLocation().getBlockY() + ","
                        + cblock.getLocation().getBlockZ() + ", facing "
                        + sign.getFacing() + " in the world "
                        + cblock.getLocation().getWorld().getName()
                        + " with the channel name " + cname
                        + " and with the type " + type + " by the player "
                        + player.getName());

        Location loc = cblock.getLocation();
        Boolean isWallSign = (cblock.getType() == Material.WALL_SIGN);
        WirelessChannel channel = WirelessRedstone.config
                .getWirelessChannel(cname);
        if (isWallSign) {
            isWallSign = true;
            if (!isValidWallLocation(cblock)) {
                player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                        WirelessRedstone.strings.playerCannotCreateReceiverOnBlock);
                return false;
            }
        } else {
            if (!isValidLocation(cblock)) {
                player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                        WirelessRedstone.strings.playerCannotCreateReceiverOnBlock);
                return false;
            }
        }

        if (channel == null) {
            WirelessRedstone
                    .getWRLogger()
                    .debug("The channel doesn't exist. Creating it and adding the receiver in it.");
            if (cname.contains(".")) {
                player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                        WirelessRedstone.strings.channelNameContainsInvalidCaracters);
                return false;
            }
            channel = new WirelessChannel(cname);
            channel.addOwner(player.getName());
            WirelessReceiver receiver;
            switch (type) {
                case Default:
                    receiver = new WirelessReceiver();
                    break;

                case Inverter:
                    receiver = new WirelessReceiverInverter();
                    break;

                case Switch:
                    String stateStr = ((Sign) (cblock.getState())).getLine(3);
                    boolean state;
                    try {
                        state = Boolean.parseBoolean(stateStr);
                    } catch (NumberFormatException ex) {
                        receiver = new WirelessReceiverSwitch();
                        break;
                    }
                    receiver = new WirelessReceiverSwitch(state);
                    break;
//                    receiver = new WirelessReceiverSwitch();
//                    break;

                case Delayer:
                    String delayStr = ((Sign) (cblock.getState())).getLine(3);
                    int delay;
                    try {
                        delay = Integer.parseInt(delayStr);
                    } catch (NumberFormatException ex) {
                        player.sendMessage(ChatColor.RED
                                + "[WirelessRedstone] The delay must be a number!");
                        return false;
                    }
                    if (delay < 50) {
                        player.sendMessage(ChatColor.RED
                                + "[WirelessRedstone] The delay must be at least 50ms");
                        return false;
                    }
                    receiver = new WirelessReceiverDelayer(delay);
                    break;

                case Clock:
                    String clockDelayStr = ((Sign) (cblock.getState()))
                            .getLine(3);
                    int clockDelay;
                    try {
                        clockDelay = Integer.parseInt(clockDelayStr);
                    } catch (NumberFormatException ex) {
                        player.sendMessage(ChatColor.RED
                                + "[WirelessRedstone] The delay must be a number!");
                        return false;
                    }
                    if (clockDelay < 50) {
                        player.sendMessage(ChatColor.RED
                                + "[WirelessRedstone] The delay must be at least 50ms");
                        return false;
                    }
                    receiver = new WirelessReceiverClock(clockDelay);
                    break;

                default:
                    receiver = new WirelessReceiver();
                    break;
            }
            receiver.setOwner(player.getName());
            receiver.setWorld(loc.getWorld().getName());
            receiver.setX(loc.getBlockX());
            receiver.setY(loc.getBlockY());
            receiver.setZ(loc.getBlockZ());
            BlockFace bfaceDirection = sign.getFacing();
            receiver.setDirection(bfaceDirection);
            receiver.setIsWallSign(isWallSign);
            channel.addReceiver(receiver);
            if (!WirelessRedstone.config.createWirelessChannel(channel)) {
                player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                        WirelessRedstone.strings.channelNameContainsInvalidCaracters);
                return false;
            }
            player.sendMessage(ChatColor.GREEN + WirelessRedstone.strings.chatTag +
                    WirelessRedstone.strings.playerCreatedChannel);
            WirelessRedstone.cache.update();
            return true;
        } else {
            WirelessRedstone.getWRLogger().debug(
                    "Channel " + cname + " exists. Adding a receiver in it.");
            if (cname.contains(".")) {
                player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                        WirelessRedstone.strings.channelNameContainsInvalidCaracters);
                return false;
            }
            WirelessReceiver receiver;
            switch (type) {
                case Default:
                    receiver = new WirelessReceiver();
                    break;

                case Inverter:
                    receiver = new WirelessReceiverInverter();
                    break;

                case Switch:
                    String stateStr = ((Sign) (cblock.getState())).getLine(3);
                    boolean state;
                    try {
                        state = Boolean.parseBoolean(stateStr);
                    } catch (NumberFormatException ex) {
                        receiver = new WirelessReceiverSwitch();
                        break;
                    }
                    receiver = new WirelessReceiverSwitch(state);
                    break;
//                    receiver = new WirelessReceiverSwitch();
//                    break;

                case Delayer:
                    String delayStr = ((Sign) (cblock.getState())).getLine(3);
                    int delay;
                    try {
                        delay = Integer.parseInt(delayStr);
                    } catch (NumberFormatException ex) {
                        player.sendMessage(ChatColor.RED
                                + "[WirelessRedstone] The delay must be a number!");
                        return false;
                    }
                    if (delay < 50) {
                        player.sendMessage(ChatColor.RED
                                + "[WirelessRedstone] The delay must be at least 50ms");
                        return false;
                    }
                    receiver = new WirelessReceiverDelayer(delay);
                    break;

                case Clock:
                    String clockDelayStr = ((Sign) (cblock.getState()))
                            .getLine(3);
                    int clockDelay;
                    try {
                        clockDelay = Integer.parseInt(clockDelayStr);
                    } catch (NumberFormatException ex) {
                        player.sendMessage(ChatColor.RED
                                + "[WirelessRedstone] The delay must be a number!");
                        return false;
                    }
                    if (clockDelay < 50) {
                        player.sendMessage(ChatColor.RED
                                + "[WirelessRedstone] The delay must be at least 50ms");
                        return false;
                    }
                    receiver = new WirelessReceiverClock(clockDelay);
                    break;

                default:
                    receiver = new WirelessReceiver();
                    break;
            }
            receiver.setOwner(player.getName());
            receiver.setWorld(loc.getWorld().getName());
            receiver.setX(loc.getBlockX());
            receiver.setY(loc.getBlockY());
            receiver.setZ(loc.getBlockZ());
            BlockFace bfaceDirection = sign.getFacing();
            receiver.setDirection(bfaceDirection);
            receiver.setIsWallSign(isWallSign);
            channel.addReceiver(receiver);
            WirelessRedstone.config.createWirelessPoint(cname, receiver);
            player.sendMessage(ChatColor.GREEN + WirelessRedstone.strings.chatTag + WirelessRedstone.strings.playerExtendedChannel);
            WirelessRedstone.cache.update();
            return true;
        }
    }

    public boolean addWirelessTransmitter(final String cname,
                                          final Block cblock, final Player player) {
        Location loc = cblock.getLocation();
        Boolean isWallSign = false;
        if (cblock.getType() == Material.WALL_SIGN) {
            isWallSign = true;
        }

        org.bukkit.material.Sign sign = (org.bukkit.material.Sign) cblock.getState().getData();

        WirelessChannel channel = WirelessRedstone.config
                .getWirelessChannel(cname);
        if (channel == null) {
            if (cname.contains(".")) {
                player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                        WirelessRedstone.strings.channelNameContainsInvalidCaracters);
                return false;
            }
            channel = new WirelessChannel(cname);
            channel.addOwner(player.getName());
            WirelessTransmitter transmitter = new WirelessTransmitter();
            transmitter.setOwner(player.getName());
            transmitter.setWorld(loc.getWorld().getName());
            transmitter.setX(loc.getBlockX());
            transmitter.setY(loc.getBlockY());
            transmitter.setZ(loc.getBlockZ());
            BlockFace bfaceDirection = sign.getFacing();
            transmitter.setDirection(bfaceDirection);
            transmitter.setIsWallSign(isWallSign);
            channel.addTransmitter(transmitter);
            if (!WirelessRedstone.config.createWirelessChannel(channel)) {
                player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                        WirelessRedstone.strings.channelNameContainsInvalidCaracters);
                return false;
            }
            player.sendMessage(ChatColor.GREEN + WirelessRedstone.strings.chatTag +
                    WirelessRedstone.strings.playerCreatedChannel);
            WirelessRedstone.cache.update();
            return true;
        } else {
            if (cname.contains(".")) {
                player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                        WirelessRedstone.strings.channelNameContainsInvalidCaracters);
                return false;
            }
            WirelessTransmitter transmitter = new WirelessTransmitter();
            transmitter.setOwner(player.getName());
            transmitter.setWorld(loc.getWorld().getName());
            transmitter.setX(loc.getBlockX());
            transmitter.setY(loc.getBlockY());
            transmitter.setZ(loc.getBlockZ());
            BlockFace bfaceDirection = sign.getFacing();
            transmitter.setDirection(bfaceDirection);
            transmitter.setIsWallSign(isWallSign);
            WirelessRedstone.config.createWirelessPoint(cname, transmitter);
            player.sendMessage(ChatColor.GREEN + WirelessRedstone.strings.chatTag +
                    WirelessRedstone.strings.playerExtendedChannel);
            WirelessRedstone.cache.update();
            return true;
        }
    }

    public boolean addWirelessScreen(final String cname, final Block cblock,
                                     final Player player) {
        Location loc = cblock.getLocation();
        Boolean isWallSign = false;
        if (cblock.getType() == Material.WALL_SIGN) {
            isWallSign = true;
        }

        org.bukkit.material.Sign sign = (org.bukkit.material.Sign) cblock.getState().getData();

        WirelessChannel channel = WirelessRedstone.config
                .getWirelessChannel(cname);

        if (channel == null) {
            if (cname.contains(".")) {
                player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                        WirelessRedstone.strings.channelNameContainsInvalidCaracters);
                return false;
            }
            channel = new WirelessChannel(cname);
            channel.addOwner(player.getName());
            WirelessScreen screen = new WirelessScreen();
            screen.setOwner(player.getName());
            screen.setWorld(loc.getWorld().getName());
            screen.setX(loc.getBlockX());
            screen.setY(loc.getBlockY());
            screen.setZ(loc.getBlockZ());
            BlockFace bfaceDirection = sign.getFacing();
            screen.setDirection(bfaceDirection);
            screen.setIsWallSign(isWallSign);
            channel.addScreen(screen);
            if (!WirelessRedstone.config.createWirelessChannel(channel)) {
                player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                        WirelessRedstone.strings.channelNameContainsInvalidCaracters);
                return false;
            }
            player.sendMessage(ChatColor.GREEN + WirelessRedstone.strings.chatTag +
                    WirelessRedstone.strings.playerCreatedChannel);
            WirelessRedstone.cache.update();
            return true;
        } else {
            if (cname.contains(".")) {
                player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                        WirelessRedstone.strings.channelNameContainsInvalidCaracters);
                return false;
            }
            if (channel instanceof WirelessChannel) {
                WirelessScreen screen = new WirelessScreen();
                screen.setOwner(player.getName());
                screen.setWorld(loc.getWorld().getName());
                screen.setX(loc.getBlockX());
                screen.setY(loc.getBlockY());
                screen.setZ(loc.getBlockZ());
                BlockFace bfaceDirection = sign.getFacing();
                screen.setDirection(bfaceDirection);
                screen.setIsWallSign(isWallSign);
                channel.addScreen(screen);
                WirelessRedstone.config.createWirelessPoint(cname, screen);
                player.sendMessage(ChatColor.GREEN + WirelessRedstone.strings.chatTag +
                        WirelessRedstone.strings.playerExtendedChannel);
                WirelessRedstone.cache.update();
                return true;
            }
        }
        return false;
    }

    /**
     * This method gets the block the sign is attached to, and checks that a
     * redstone torch can be put on the side of this block.
     *
     * @param block - The sign block
     * @return true if a torch can be put there.
     */
    public boolean isValidWallLocation(final Block block) {
        org.bukkit.material.Sign sign = (org.bukkit.material.Sign) block
                .getState().getData();
        BlockFace face = sign.getAttachedFace();
        Block tempBlock = block.getRelative(face);

        Boolean check = !(tempBlock.getType() == Material.AIR
                || tempBlock.getType() == Material.PISTON_BASE
                || tempBlock.getType() == Material.PISTON_EXTENSION
                || tempBlock.getType() == Material.PISTON_MOVING_PIECE
                || tempBlock.getType() == Material.PISTON_STICKY_BASE
                || tempBlock.getType() == Material.GLOWSTONE
                || tempBlock.getType() == Material.REDSTONE_LAMP_ON
                || tempBlock.getType() == Material.REDSTONE_LAMP_OFF
                || tempBlock.getType() == Material.LEAVES
                || tempBlock.getType() == Material.WOOD_STAIRS
                || tempBlock.getType() == Material.COBBLESTONE_STAIRS
                || tempBlock.getType() == Material.RED_SANDSTONE_STAIRS
                || tempBlock.getType() == Material.SANDSTONE_STAIRS
                || tempBlock.getType() == Material.FENCE
                || tempBlock.getType() == Material.ACACIA_FENCE
                || tempBlock.getType() == Material.DARK_OAK_FENCE
                || tempBlock.getType() == Material.JUNGLE_FENCE
                || tempBlock.getType() == Material.BIRCH_FENCE
                || tempBlock.getType() == Material.WOOD_DOOR
                || tempBlock.getType() == Material.WOODEN_DOOR
                || tempBlock.getType() == Material.IRON_DOOR_BLOCK
                || tempBlock.getType() == Material.IRON_DOOR
                || tempBlock.getType() == Material.GLASS
                || tempBlock.getType() == Material.THIN_GLASS
                || tempBlock.getType() == Material.STAINED_GLASS
                || tempBlock.getType() == Material.STAINED_GLASS_PANE
                || tempBlock.getType() == Material.COBBLE_WALL
                || tempBlock.getType() == Material.ICE
                || tempBlock.getType() == Material.WOOD_STEP
                || tempBlock.getType() == Material.STEP
                || tempBlock.getType() == Material.TNT);
        if(WirelessRedstone.getBukkitVersion().contains("v1_8")){
            if(check) check = !(tempBlock.getType() == Material.SEA_LANTERN);
        }
        return check;
    }

    public boolean isValidLocation(final Block block) {
        if (block == null)
            return false;

        Block tempBlock = block.getRelative(BlockFace.DOWN);

        Boolean check = !(tempBlock.getType() == Material.AIR
                || tempBlock.getType() == Material.PISTON_BASE
                || tempBlock.getType() == Material.PISTON_EXTENSION
                || tempBlock.getType() == Material.PISTON_MOVING_PIECE
                || tempBlock.getType() == Material.PISTON_STICKY_BASE
                || tempBlock.getType() == Material.GLOWSTONE
                || tempBlock.getType() == Material.REDSTONE_LAMP_ON
                || tempBlock.getType() == Material.REDSTONE_LAMP_OFF
                || tempBlock.getType() == Material.LEAVES
                || tempBlock.getType() == Material.TNT);
        if(WirelessRedstone.getBukkitVersion().contains("v1_8")){
            if(check) check = !(tempBlock.getType() == Material.SEA_LANTERN);
        }
        return check;
    }

    public ArrayList<Location> getReceiverLocations(
            final WirelessChannel channel) {
        ArrayList<Location> returnlist = new ArrayList<Location>();
        for (WirelessReceiver receiver : channel.getReceivers()) {
            returnlist.add(receiver.getLocation());
        }
        return returnlist;
    }

    public ArrayList<Location> getReceiverLocations(final String channelname) {
        WirelessChannel channel = WirelessRedstone.config
                .getWirelessChannel(channelname);
        if (channel == null)
            return new ArrayList<Location>();

        return getReceiverLocations(channel);
    }

    public ArrayList<Location> getScreenLocations(final WirelessChannel channel) {
        ArrayList<Location> returnlist = new ArrayList<Location>();
        for (WirelessScreen screen : channel.getScreens()) {
            returnlist.add(screen.getLocation());
        }
        return returnlist;
    }

    public ArrayList<Location> getScreenLocations(final String channelname) {
        WirelessChannel channel = WirelessRedstone.config
                .getWirelessChannel(channelname);
        if (channel == null)
            return new ArrayList<Location>();

        return getScreenLocations(channel);
    }

    public void removeReceiverAt(final Location loc, final boolean byplayer) {
        plugin.getServer().getScheduler()
                .runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        for (WirelessChannel channel : WirelessRedstone.config
                                .getAllChannels()) {
                            for (WirelessReceiver receiver : channel
                                    .getReceivers()) {
                                if (receiver.getX() == loc.getBlockX()
                                        && receiver.getY() == loc.getBlockY()
                                        && receiver.getZ() == loc.getBlockZ()) {
                                    WirelessRedstone.config
                                            .removeWirelessReceiver(
                                                    channel.getName(), loc);
                                    if (!byplayer) {
                                        for (String owner : channel.getOwners()) {
                                            try {
                                                if (plugin.getServer()
                                                        .getPlayer(owner)
                                                        .isOnline()) {
                                                    plugin.getServer()
                                                            .getPlayer(owner)
                                                            .sendMessage(
                                                                    "One of your signs on channel: "
                                                                            + channel
                                                                            .getName()
                                                                            + " is broken by nature.");
                                                }
                                            } catch (Exception ex) {
                                                // NA
                                            }
                                        }
                                    }
                                    return;
                                }
                            }
                        }
                    }
                });
    }

    public boolean removeWirelessReceiver(final String cname, final Location loc) {
        if (WirelessRedstone.config.removeWirelessReceiver(cname, loc)) {
            WirelessRedstone.cache.update();
            return true;
        } else
            return false;
    }

    public boolean removeWirelessTransmitter(final String cname,
                                             final Location loc) {
        if (WirelessRedstone.config.removeWirelessTransmitter(cname, loc)) {
            WirelessRedstone.cache.update();
            return true;
        } else
            return false;
    }

    public boolean removeWirelessScreen(final String cname, final Location loc) {
        if (WirelessRedstone.config.removeWirelessScreen(cname, loc)) {
            WirelessRedstone.cache.update();
            return true;
        } else
            return false;
    }

    public void removeSigns(final WirelessChannel channel) {
        try {
            for (IWirelessPoint point : channel.getReceivers()) {
                point.getLocation().getBlock().setType(Material.AIR);
            }
        } catch (NullPointerException ex) {
            // When there isn't any receiver, it'll throw this exception.
        }

        try {
            for (IWirelessPoint point : channel.getTransmitters()) {
                point.getLocation().getBlock().setType(Material.AIR);
            }
        } catch (NullPointerException ex) {
            // When there isn't any transmitter, it'll throw this exception.
        }

        try {
            for (IWirelessPoint point : channel.getScreens()) {
                point.getLocation().getBlock().setType(Material.AIR);
            }
        } catch (NullPointerException ex) {
            // When there isn't any screen, it'll throw this exception.
        }
    }

    public void signWarning(final Block block, final int code) {
        Sign sign = (Sign) block.getState();
        switch (code) {
            case 1:
                sign.setLine(2, "Bad block");
                sign.setLine(3, "Behind sign");
                sign.update();
                break;

            default:
                break;
        }
    }
}