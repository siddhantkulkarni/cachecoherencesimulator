/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cachecoherencesimulator;

/**
 *
 * @author Siddhant Kulkarni
 */
public class Exec {

    public static int getID(String temp) {
        String[] arr = temp.split("-");
        return Integer.parseInt(arr[1]);
    }
    public static Processor[] processors;
    public static SharedMemory ram;

    public static void executeScenario(String scenario) throws InterruptedException {
        SimulatorWindow.setControllerState(false);
        processors = new Processor[UserChoices.numberOfProcessors];
        for (int i = 0; i < UserChoices.numberOfProcessors; i++) {
            processors[i] = new Processor(UserChoices.numberOfBlocksLocal);
        }
        ram = new SharedMemory(UserChoices.numberOfBlocksMain);
        UserChoices.updateProtocolObject();
        String[] ops = scenario.split(",");
        for (int i = 0; i < ops.length; i++) {
            String[] spec = ops[i].split(":");
            int procId = Exec.getID(spec[0]);
            int blockId = Exec.getID(spec[1]);
            for (int k = 0; k < processors[procId].localCache.length; k++) {
                if (processors[procId].localCache[k].blockID != blockId) {
                    processors[procId].localCache[k].lastUsed++;
                }
            }
            String op = spec[2];
            double val = 0;
            if (op.equals("W")) {
                val = Double.parseDouble(spec[3]);
            }

            if (op.equals("R")) {
                Bus.readBlock(processors, procId, blockId);
            } else {
                Bus.writeBlock(processors, procId, blockId, val);
            }
            SimulatorWindow.updateSequenceExecutionStatus();
            Thread.sleep(500);
        }
        for(int x=0;x<processors.length;x++){
            for(int z=0;z<processors[x].localCache.length;z++){
                if(processors[x].localCache[z].blockState==StateEnum.D ||
                        processors[x].localCache[z].blockState==StateEnum.M ||
                        processors[x].localCache[z].blockState==StateEnum.SD){
                    ram.values[processors[x].localCache[z].blockID]=processors[x].localCache[z].value;
                    SimulatorWindow.evaluator.cntWriteBacks++;
                }
                
            }
        }
        SimulatorWindow.setControllerState(true);
        SimulatorWindow.updateEvalParamBox();
    }

    public static boolean isThisBlockExclusive(int block) {
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

    public static LocalCacheBlock getBlock(int procID, int blockID) {
        for (int i = 0; i < processors[procID].localCache.length; i++) {
            if (blockID == processors[procID].localCache[i].blockID) {
                return processors[procID].localCache[i];
            }
        }
        return null;
    }
}
