/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cachecoherencesimulator;

/**
 *
 * @author Team 4 - Siddhant Kulkarni, Ritesh Sangurmath, Ranjan Yadav
 * 
 * This class simulates the messages being sent on the bus by executing Local and Remote Writes
 */
public class Bus {
    public static double val;
    public static void readBlock(Processor[] processors,int proc, int block){
        boolean isShared=false;
        for(int i=0;i<processors.length;i++){//first we go through all other proc
            if(proc==i){
                continue;
            }else{
                if(processors[i].containsBlock(block)){// checks if the block is present in the local cache of the processor
                    UserChoices.useProtocol.remoteRead(i, block);// perfrom the remote read operation 
                    isShared=true;
                }
            }
        }
        UserChoices.useProtocol.localRead(proc, block, isShared);// perform the local read operation
    }
    public static void writeBlock(Processor[] processors,int proc, int block, double val){
        boolean isShared=false;
        for(int i=0;i<processors.length;i++){//first we go through all other proc
            if(proc==i){
                continue;
            }else{
                if(processors[i].containsBlock(block)){// checks if the block is present in the loacl cache of the processor
                    UserChoices.useProtocol.remoteWrite(i, block,val);// perform remote write operation 
                    isShared=true;
                }
            }
        }
        //perform actual writes here
        UserChoices.useProtocol.localWrite(proc, block, isShared);// perform the local write
        int ind=processors[proc].getBlockIndex(block);
        Bus.val=val;
        processors[proc].localCache[ind].value=val;
        
        
    }
}
