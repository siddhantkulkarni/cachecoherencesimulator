/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cachecoherencesimulator.protocols;

import cachecoherencesimulator.*;

/**
 *
 * @author Team 4 - Siddhant Kulkarni, Ritesh Sangurmath, Ranjan Yadav
 */
public class MSI implements ProtocolInterface {

    @Override
    public void localRead(int procID, int blockID, boolean isShared) {

        SimulatorWindow.evaluator.cntMessagesOnBus++;
        //System.out.println("Proc "+procID+" is reading block "+blockID);
        if (!Exec.processors[procID].containsBlock(blockID)) {// checks if the block is not present in the loacl cache of the processor
            //cache read miss
            SimulatorWindow.evaluator.cacheReadMisses++;// read miss will be incremented as block is not present in the local cache
            SimulatorWindow.evaluator.cntReadsFromRam++;
            // System.out.println("Read miss proc:"+procID+" block:"+blockID);
            int loc = Exec.processors[procID].getALocationToPlaceBlock();// get the space in the local cache to accomodate the new block from main memory
            if (Exec.processors[procID].localCache[loc].isOccupied && Exec.processors[procID].localCache[loc].blockState==StateEnum.M) {// checks if the block is in modify state 
                Exec.ram.values[Exec.processors[procID].localCache[loc].blockID] = Exec.processors[procID].localCache[loc].value;// get the new block from the main memory
                SimulatorWindow.evaluator.cntWriteBacks++;//perfrom the write back operation
            }
            Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.S, Exec.ram.values[blockID], 0, true);// create the new block in the local cache of the processor
            return;
        }
        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);// selects the block from the local cache of the processor

        switch (tempBlock.blockState) {
            case M://block will be be in modify state
                break;
            case S:// block will be in shared state
                break;
            case I:
                //coherence read miss
                SimulatorWindow.evaluator.coherenceReadMisses++;
                int loc = Exec.processors[procID].getBlockIndex(blockID);
                Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.S, Exec.ram.values[blockID], 0, true);// block will change the state to shared state and gets the block from the main memory
                SimulatorWindow.evaluator.cntReadsFromRam++;
                break;
        }
    }

    @Override
    public void localWrite(int procID, int blockID, boolean isShared) {
        SimulatorWindow.evaluator.cntMessagesOnBus++;
        if (!Exec.processors[procID].containsBlock(blockID)) {//checks if the block is present in the local cache of the processor
            //cache write miss
            SimulatorWindow.evaluator.cacheWriteMisses++;
            SimulatorWindow.evaluator.cntReadsFromRam++;
            int loc = Exec.processors[procID].getALocationToPlaceBlock();// get the new location for the block that is fetched from the main memory
            if (Exec.processors[procID].localCache[loc].isOccupied && Exec.processors[procID].localCache[loc].blockState==StateEnum.M) {//checks if the block is in modify state
                Exec.ram.values[Exec.processors[procID].localCache[loc].blockID] = Exec.processors[procID].localCache[loc].value;// perfrom write back operation
                SimulatorWindow.evaluator.cntWriteBacks++;
            }
            Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.M, Exec.ram.values[blockID], 0, true);// create the new block in the local cache
            return;
        }
        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);// selects the block from the local cache of the processor

        switch (tempBlock.blockState) {
            case M:// block will be in modify state
                break;
            case S:
                tempBlock.blockState = StateEnum.M;// block will change its state to modify state from shared state
                break;
            case I:
                //coherence read miss
                SimulatorWindow.evaluator.coherenceWriteMisses++;
                SimulatorWindow.evaluator.cntReadsFromRam++;
                int loc = Exec.processors[procID].getBlockIndex(blockID);
                Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.M, Exec.ram.values[blockID], 0, true);// cache will change the state to modify state
                break;
        }
    }

    @Override
    public void remoteRead(int procID, int blockID) {
        if (!Exec.processors[procID].containsBlock(blockID)) {// checks if the block is present in the local cache of the processor
            return;
        }
        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);// selects the block from the local cache of the processor
        switch (tempBlock.blockState) {
            case M:
                //writeback
                Exec.ram.values[tempBlock.blockID] = tempBlock.value;
                tempBlock.blockState = StateEnum.S;// block will change the state from modify to shared
                SimulatorWindow.evaluator.cntWriteBacks++;
                break;
            case S:// block will be in shared state
                break;
            case I:// block will be in invalid state
                break;
        }
    }

    @Override
    public void remoteWrite(int procID, int blockID, double value) {
        if (!Exec.processors[procID].containsBlock(blockID)) {//check if the block is present in the local cache of the processor
            return;
        }
        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);// selects the block from the local cache of the processor
        switch (tempBlock.blockState) {
            case M:
                //writeback
                Exec.ram.values[tempBlock.blockID] = tempBlock.value;
                tempBlock.blockState = StateEnum.I;// the block will change its state from modify to invalid state
                SimulatorWindow.evaluator.entriesToInvalid++;
                SimulatorWindow.evaluator.cntWriteBacks++;
                break;
            case S:
                tempBlock.blockState = StateEnum.I;// block will change its state from shared to invalid
                SimulatorWindow.evaluator.entriesToInvalid++;
                break;
            case I:// block will be in invalid state
                break;
        }
    }

}
