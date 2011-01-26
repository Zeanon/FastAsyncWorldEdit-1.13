// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.scripting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.sk89q.worldedit.DisallowedItemException;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.InsufficientArgumentsException;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.UnknownItemException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.patterns.Pattern;

/**
 * The context given to scripts.
 * 
 * @author sk89q
 */
public class CraftScriptContext extends CraftScriptEnvironment {
    private List<EditSession> editSessions = new ArrayList<EditSession>();
    private String[] args;
    
    public CraftScriptContext(WorldEdit controller,
            ServerInterface server, LocalConfiguration config,
            LocalSession session, LocalPlayer player, String[] args) {
        super(controller, server, config, session, player);
        this.args = args;
    }
    
    /**
     * Get an edit session. Every subsequent call returns a new edit session.
     * Usually you only need to use one edit session.
     * 
     * @return
     */
    public EditSession remember() {
        EditSession editSession =
                new EditSession(server, player.getWorld(),
                        session.getBlockChangeLimit(), session.getBlockBag(player));
        editSessions.add(editSession);
        return editSession;
    }
    
    /**
     * Get the player.
     * 
     * @return
     */
    public LocalPlayer getPlayer() {
        return player;
    }
    
    /**
     * Get the player's session.
     * 
     * @return
     */
    public LocalSession getSession() {
        return session;
    }
    
    /**
     * Get the configuration for WorldEdit.
     * 
     * @return
     */
    public LocalConfiguration getConfiguration() {
        return config;
    }
    
    /**
     * Get a list of edit sessions that have been created.
     * 
     * @return
     */
    public List<EditSession> getEditSessions() {
        return Collections.unmodifiableList(editSessions);
    }
    
    /**
     * Print a regular message to the user.
     * 
     * @param msg
     */
    public void print(String msg) {
        player.print(msg);
    }
    
    /**
     * Print an error message to the user.
     * 
     * @param msg
     */
    public void error(String msg) {
        player.printError(msg);
    }
    
    /**
     * Print an raw message to the user.
     * 
     * @param msg
     */
    public void printRaw(String msg) {
        player.printRaw(msg);
    }

    /**
     * Checks to make sure that there are enough but not too many arguments.
     *
     * @param args
     * @param min
     * @param max -1 for no maximum
     * @param usage usage string
     * @throws InsufficientArgumentsException
     */
    public void checkArgs(int min, int max, String usage)
            throws InsufficientArgumentsException {
        if (args.length <= min || (max != -1 && args.length - 1 > max)) {
            throw new InsufficientArgumentsException("Usage: " + usage);
        }
    }

    /**
     * Get an item ID from an item name or an item ID number.
     *
     * @param arg
     * @param allAllowed true to ignore blacklists
     * @return
     * @throws UnknownItemException
     * @throws DisallowedItemException
     */
    public BaseBlock getBlock(String arg, boolean allAllowed)
            throws UnknownItemException, DisallowedItemException {
        return controller.getBlock(player, arg, allAllowed);
    }

    /**
     * Get a block.
     *
     * @param id
     * @return
     * @throws UnknownItemException
     * @throws DisallowedItemException
     */
    public BaseBlock getBlock(String id)
            throws UnknownItemException, DisallowedItemException {
        return controller.getBlock(player, id, false);
    }
    
    /**
     * Get a list of blocks as a set. This returns a Pattern.
     *
     * @param list
     * @return pattern
     */
    public Pattern getBlockPattern(String list)
            throws UnknownItemException, DisallowedItemException {
        return controller.getBlockPattern(player, list);
    }

    /**
     * Get a list of blocks as a set.
     *
     * @param list
     * @param allBlocksAllowed
     * @return set
     */
    public Set<Integer> getBlockIDs(String list, boolean allBlocksAllowed)
            throws UnknownItemException, DisallowedItemException {
        return controller.getBlockIDs(player, list, allBlocksAllowed);
    }
    
    /**
     * Gets the path to a file. This method will check to see if the filename
     * has valid characters and has an extension. It also prevents directory
     * traversal exploits by checking the root directory and the file directory.
     * On success, a <code>java.io.File</code> object will be returned,
     * otherwise a null will be returned and the player will be informed.
     * 
     * <p>Use this method if you need to read a file from a directory.</p>
     * 
     * @param folder subdirectory to look in
     * @param filename filename (user-submitted)
     * @return
     */
    public File getSafeFile(String folder, String filename) {
        File dir = new File(folder);
        File f = new File(dir, filename);

        if (!filename.matches("^[A-Za-z0-9_\\- \\./\\\\'\\$@~!%\\^\\*\\(\\)\\[\\]\\+\\{\\},\\?]+\\.[A-Za-z0-9]+$")) {
            player.printError("Invalid filename specified.");
            return null;
        }

        try {
            String filePath = f.getCanonicalPath();
            String dirPath = dir.getCanonicalPath();

            if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
                player.printError("File could not read or it does not exist.");
                return null;
            }
            
            return f;
        } catch (IOException e) {
            player.printError("File could not read or it does not exist: " + e.getMessage());
            return null;
        }
    }
}
