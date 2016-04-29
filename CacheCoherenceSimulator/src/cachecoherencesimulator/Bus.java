/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cachecoherencesimulator;

/**
 *
 * @author Siddhant Kulkarni
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
                if(processors[i].containsBlock(block)){
                    UserChoices.useProtocol.remoteRead(i, block);
                    isShared=true;
                }
            }
        }
        UserChoices.useProtocol.localRead(proc, block, isShared);
    }
    public static void writeBlock(Processor[] processors,int proc, int block, double val){
        boolean isShared=false;
        for(int i=0;i<processors.length;i++){//first we go through all other proc
            if(proc==i){
                continue;
            }else{
                if(processors[i].containsBlock(block)){
                    UserChoices.useProtocol.remoteWrite(i, block,val);
                    isShared=true;
                }
            }
        }
        //perform actual writes here
        UserChoices.useProtocol.localWrite(proc, block, isShared);
        int ind=processors[proc].getBlockIndex(block);
        Bus.val=val;
        processors[proc].localCache[ind].value=val;
        
        
    }
}
