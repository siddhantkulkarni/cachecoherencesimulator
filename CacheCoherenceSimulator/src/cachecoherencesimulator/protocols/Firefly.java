/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cachecoherencesimulator.protocols;

import cachecoherencesimulator.*;

/**
 *
 * @author Siddhant Kulkarni
 */
public class Firefly implements ProtocolInterface {

    @Override
    public void localRead(int procID, int blockID, boolean isShared) {
        SimulatorWindow.evaluator.cntMessagesOnBus++;

        if (!Exec.processors[procID].containsBlock(blockID)) {
            //cache read miss
            SimulatorWindow.evaluator.cacheReadMisses++;

            // System.out.println("Read miss proc:"+procID+" block:"+blockID);
            int loc = Exec.processors[procID].getALocationToPlaceBlock();
            if (Exec.processors[procID].localCache[loc].isOccupied && (Exec.processors[procID].localCache[loc].blockState == StateEnum.D)) {
                Exec.ram.values[Exec.processors[procID].localCache[loc].blockID] = Exec.processors[procID].localCache[loc].value;
                SimulatorWindow.evaluator.cntWriteBacks++;
            }
            double temp = Double.MIN_VALUE;
            for (int x = 0; x < Exec.processors.length; x++) {
                for (int z = 0; z < Exec.processors[x].localCache.length; z++) {
                    if (Exec.processors[x].localCache[z].blockID == blockID) {
                        temp = Exec.processors[x].localCache[z].value;
                    }
                }
            }
            if (temp != Double.MIN_VALUE) {

                if (isShared) {
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.S, temp, 0, true);
                } else {
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.VE, temp, 0, true);
                }
            } else {
                SimulatorWindow.evaluator.cntReadsFromRam++;
                // System.out.println("!!");
                if (isShared) {
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.S, Exec.ram.values[blockID], 0, true);
                } else {
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.VE, Exec.ram.values[blockID], 0, true);
                }
            }

            return;
        }

        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);

        switch (tempBlock.blockState) {
            case VE:
                if (isShared) {
                    tempBlock.blockState = StateEnum.S;
                } else {
                    SimulatorWindow.evaluator.cntMessagesOnBus--;
                }
                break;
            case S:
                if (!isShared) {
                    tempBlock.blockState = StateEnum.VE;
                }
                break;
            case D:
                break;
        }
    }

    @Override
    public void localWrite(int procID, int blockID, boolean isShared) {
        SimulatorWindow.evaluator.cntMessagesOnBus+=2;
        if (!Exec.processors[procID].containsBlock(blockID)) {
            //cache write miss
            SimulatorWindow.evaluator.cacheWriteMisses++;
           // SimulatorWindow.evaluator.cntReadsFromRam++;
            int loc = Exec.processors[procID].getALocationToPlaceBlock();
            if (Exec.processors[procID].localCache[loc].isOccupied && (Exec.processors[procID].localCache[loc].blockState == StateEnum.D)) {
                Exec.ram.values[Exec.processors[procID].localCache[loc].blockID] = Exec.processors[procID].localCache[loc].value;
                SimulatorWindow.evaluator.cntWriteBacks++;
            }
            double temp = Double.MIN_VALUE;
            for (int x = 0; x < Exec.processors.length; x++) {
                for (int z = 0; z < Exec.processors[x].localCache.length; z++) {
                    if (Exec.processors[x].localCache[z].blockID == blockID) {
                        temp = Exec.processors[x].localCache[z].value;
                    }
                }
            }
            if (temp != Double.MIN_VALUE) {

                Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.D, temp, 0, true);

            } else {
                SimulatorWindow.evaluator.cntReadsFromRam++;
                Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.D, Exec.ram.values[blockID], 0, true);

            }

            for (int x = 0; x < Exec.processors.length; x++) {
                for (int z = 0; z < Exec.processors[x].localCache.length; z++) {
                    if (Exec.processors[x].localCache[z].blockID == blockID) {
                        Exec.processors[x].localCache[z].value = Bus.val;
                    }
                }
            }
            return;
        }
        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);

        switch (tempBlock.blockState) {
            case VE:
                tempBlock.blockState = StateEnum.D;
                SimulatorWindow.evaluator.cntMessagesOnBus--;
                break;
            case S:
                tempBlock.blockState = StateEnum.D;
                break;
            case D:

                for (int x = 0; x < Exec.processors.length; x++) {
                    for (int z = 0; z < Exec.processors[x].localCache.length; z++) {
                        if (Exec.processors[x].localCache[z].blockID == blockID) {
                            Exec.processors[x].localCache[z].value = Bus.val;
                        }
                    }
                }
                break;

        }
    }

    @Override
    public void remoteRead(int procID, int blockID) {
        if (!Exec.processors[procID].containsBlock(blockID)) {
            return;
        }
        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);
        switch (tempBlock.blockState) {
            case VE:
                tempBlock.blockState = StateEnum.S;
                break;
            case S:
                break;
            case D:
                tempBlock.blockState = StateEnum.S;
                break;

        }
    }

    @Override
    public void remoteWrite(int procID, int blockID, double value) {
        if (!Exec.processors[procID].containsBlock(blockID)) {
            return;
        }
        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);
        switch (tempBlock.blockState) {
            case VE:
                tempBlock.blockState = StateEnum.S;
                break;
            case S:
                break;
            case D:
                tempBlock.blockState = StateEnum.S;
                break;

        }
    }

}
