/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cachecoherencesimulator;

/**
 *
 * @author Team 4 - Siddhant Kulkarni, Ritesh Sangurmath, Ranjan Yadav
 */
public class Exec {

    public static int getID(String temp) {
        String[] arr = temp.split("-");
        return Integer.parseInt(arr[1]);
    }
    public static Processor[] processors;
    public static SharedMemory ram;

    public static void executeScenario(String scenario) throws InterruptedException {
        SimulatorWindow.setControllerState(false);// disable all the text field during the excution 
        processors = new Processor[UserChoices.numberOfProcessors];// create the number of processor object based upon the number of processor entered
        for (int i = 0; i < UserChoices.numberOfProcessors; i++) {
            processors[i] = new Processor(UserChoices.numberOfBlocksLocal);// asign the number of blocks entered to each processor
        }
        ram = new SharedMemory(UserChoices.numberOfBlocksMain);// assign the number of block to main memory
        UserChoices.updateProtocolObject();
        String[] ops = scenario.split(",");// separate each operation from the sequence and add it to the array "ops"
        for (int i = 0; i < ops.length; i++) {
            String[] spec = ops[i].split(":");// splits the each operation by processor number, block number, operation selected and the value for write operation
            int procId = Exec.getID(spec[0]);// gets the processor ID
            int blockId = Exec.getID(spec[1]);// gets the block ID
            for (int k = 0; k < processors[procId].localCache.length; k++) {
                if (processors[procId].localCache[k].blockID != blockId) {// checks if the blcokID given is not present in the processor local cache
                    processors[procId].localCache[k].lastUsed++;// if above condition is true then the lastused variable will be incremented
                }
            }
            String op = spec[2];
            double val = 0;
            if (op.equals("W")) {
                val = Double.parseDouble(spec[3]);// if the write operation is selected then the value that has to be written will be selected 
            }

            if (op.equals("R")) {
                Bus.readBlock(processors, procId, blockId);// perfrom read operation
            } else {
                Bus.writeBlock(processors, procId, blockId, val);// perfrom write operation
            }
            SimulatorWindow.updateSequenceExecutionStatus();
            Thread.sleep(500);// use to set the delay in excuting
        }
        for(int x=0;x<processors.length;x++){
            for(int z=0;z<processors[x].localCache.length;z++){
                if(processors[x].localCache[z].blockState==StateEnum.D ||
                        processors[x].localCache[z].blockState==StateEnum.M ||
                        processors[x].localCache[z].blockState==StateEnum.SD){
                    ram.values[processors[x].localCache[z].blockID]=processors[x].localCache[z].value;//peforms write back operation when the block is in dirty state or modify state or shared dirty state
                    SimulatorWindow.evaluator.cntWriteBacks++;// when write back operation is performed the write back variable will be incremented 
                }
                
            }
        }
        SimulatorWindow.setControllerState(true);// the text fields in the simulator will be enabled when the excution is finished
        SimulatorWindow.updateEvalParamBox();
    }

    public static boolean isThisBlockExclusive(int block) {// check if the block is exclusive
        System.out.println("" + Exec.processors.length);
        for (int i = 0; i < Exec.processors.length; i++) {
            for (int j = 0; j < Exec.processors[i].localCache.length; j++) {
                if (Exec.processors[i].localCache[j].isOccupied && Exec.processors[i].localCache[j].blockID == block) {
                    System.out.println("Block is not Exclusive");
                    return false;
                }
            }
        }
        System.out.println("Block is Exclusive");
        return true;
    }

    public static LocalCacheBlock getBlock(int procID, int blockID) {// get the block id
        for (int i = 0; i < processors[procID].localCache.length; i++) {
            if (blockID == processors[procID].localCache[i].blockID) {
                return processors[procID].localCache[i];
            }
        }
        return null;
    }
}
