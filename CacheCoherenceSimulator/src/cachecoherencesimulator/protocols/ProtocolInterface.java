/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cachecoherencesimulator.protocols;

/**
 *
 * @author Team 4 - Siddhant Kulkarni, Ritesh Sangurmath, Ranjan Yadav
 */
public interface ProtocolInterface{
    public int numberOfStates=0;
    public void localRead(int procID,int blockID,boolean isShared);
    public void localWrite(int procID,int blockID, boolean isShared);
    public void remoteRead(int procID, int blockID);
    public void remoteWrite(int procID, int blockID, double value);
}