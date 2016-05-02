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
public class EvalParam {
    public int cacheReadMisses;
    public int cacheWriteMisses;
    public int coherenceReadMisses;
    public int coherenceWriteMisses;
    public int entriesToInvalid;
    public int cntMessagesOnBus;
    public int cntReadsFromRam;
    public int cntWriteBacks;
    
}
