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
public class MOSI implements ProtocolInterface {

    @Override
    public void localRead(int procID, int blockID, boolean isShared) {
        SimulatorWindow.evaluator.cntMessagesOnBus++;

        if (!Exec.processors[procID].containsBlock(blockID)) {// checks if the block is not present in the loacl cache of the processor
            //cache read miss
            SimulatorWindow.evaluator.cacheReadMisses++;
            SimulatorWindow.evaluator.cntReadsFromRam++;
            // System.out.println("Read miss proc:"+procID+" block:"+blockID);
            int loc = Exec.processors[procID].getALocationToPlaceBlock();// get the space to alloacte the new block from the main memory
            if (Exec.processors[procID].localCache[loc].isOccupied && Exec.processors[procID].localCache[loc].blockState==StateEnum.M) {//checks if the block state is in modify state
                Exec.ram.values[Exec.processors[procID].localCache[loc].blockID] = Exec.processors[procID].localCache[loc].value;// perfrom the write back operation
                SimulatorWindow.evaluator.cntWriteBacks++;
            }
            if (!isShared) {
                Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.O, Exec.ram.values[blockID], 0, true);// block state will be in the owned state and get the block from the main memory
            } else {
                Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.S, Exec.ram.values[blockID], 0, true);// block state will be in the shared state and get the block from the main memory
            }

            return;
        }
        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);// if the block is present in the loacl cache of the processor

        switch (tempBlock.blockState) {
            case M:// block will be in the modify state

                break;
            case O:// block will be in the owned state
                SimulatorWindow.evaluator.cntMessagesOnBus--;
                break;
            case S://block will be in the shared state

                break;
            case I:
                //coherence read miss
                SimulatorWindow.evaluator.coherenceReadMisses++;
                int loc = Exec.processors[procID].getBlockIndex(blockID);
                Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.S, Exec.ram.values[blockID], 0, true);// block will be in the shared state and get the block from the main memory
                SimulatorWindow.evaluator.cntReadsFromRam++;
                break;
        }
    }

    @Override
    public void localWrite(int procID, int blockID, boolean isShared) {
        SimulatorWindow.evaluator.cntMessagesOnBus++;
        if (!Exec.processors[procID].containsBlock(blockID)) {// checks if the block is not present in the local cache of the processor
            //cache write miss
            SimulatorWindow.evaluator.cacheWriteMisses++;
            SimulatorWindow.evaluator.cntReadsFromRam++;
            int loc = Exec.processors[procID].getALocationToPlaceBlock();// get the new space for the block from the main memory
            if (Exec.processors[procID].localCache[loc].isOccupied && Exec.processors[procID].localCache[loc].blockState==StateEnum.M) {// checks if the block is in the modify state
                Exec.ram.values[Exec.processors[procID].localCache[loc].blockID] = Exec.processors[procID].localCache[loc].value;// perfrom the write back operation
                SimulatorWindow.evaluator.cntWriteBacks++;
            }
            Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.M, Exec.ram.values[blockID], 0, true);// block will be in modify state
            return;
        }
        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);// selects the block from the local cache of the processor

        switch (tempBlock.blockState) {
            case M:// block will be in the modify state
                break;
            case O:
                tempBlock.blockState = StateEnum.M;// block will change its state to modify state
                SimulatorWindow.evaluator.cntMessagesOnBus--;
                break;
            case S:
                tempBlock.blockState = StateEnum.M;// block will change its state to modify state
                break;
            case I:
                //coherence read miss
                SimulatorWindow.evaluator.coherenceWriteMisses++;
                SimulatorWindow.evaluator.cntReadsFromRam++;
                int loc = Exec.processors[procID].getBlockIndex(blockID);
                Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.M, Exec.ram.values[blockID], 0, true);// block will change its state to modify and get the value from the main memory
                break;
        }
    }

    @Override
    public void remoteRead(int procID, int blockID) {
        if (!Exec.processors[procID].containsBlock(blockID)) {// checks if the block is not present in the local cache of the processor
            return;
        }
        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);// selects the block from the local cache of the processor
        switch (tempBlock.blockState) {
            case M:
                //writeback
                Exec.ram.values[tempBlock.blockID] = tempBlock.value;
                tempBlock.blockState = StateEnum.S;// block will be in the shared state
                SimulatorWindow.evaluator.cntWriteBacks++;
                break;
            case O:
                SimulatorWindow.evaluator.cntReadsFromRam--;
                tempBlock.blockState = StateEnum.S;// block will change its state to shared state
                break;
            case S:// block will be in shared state
                break;
            case I:// block will be in the invalid state
                break;
        }
    }

    @Override
    public void remoteWrite(int procID, int blockID, double value) {
        if (!Exec.processors[procID].containsBlock(blockID)) {// checks if the block is not present in the local cache of the processor
            return;
        }
        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);// selects the block from the local cache of the processor
        switch (tempBlock.blockState) {
            case M:// 
                //writeback
                Exec.ram.values[tempBlock.blockID] = tempBlock.value;
                tempBlock.blockState = StateEnum.I;// block will change its state to invalid state
                SimulatorWindow.evaluator.entriesToInvalid++;
                SimulatorWindow.evaluator.cntWriteBacks++;
                break;
            case O:
                SimulatorWindow.evaluator.cntReadsFromRam--;
                tempBlock.blockState = StateEnum.I;// block will change its state to invalid state
                SimulatorWindow.evaluator.entriesToInvalid++;
                break;
            case S:
                tempBlock.blockState = StateEnum.I;// block will change its state to invalid state
                SimulatorWindow.evaluator.entriesToInvalid++;
                break;
            case I:// block will be in the invalid state
                break;
        }
    }

}
