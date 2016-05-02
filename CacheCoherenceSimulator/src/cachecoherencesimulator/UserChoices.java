/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cachecoherencesimulator;

import cachecoherencesimulator.protocols.ProtocolEnum;
import cachecoherencesimulator.protocols.*;

/**
 *
 * @author Team 4 - Siddhant Kulkarni, Ritesh Sangurmath, Ranjan Yadav
 */
public class UserChoices {

    public static int numberOfProcessors = 2;
    public static int numberOfBlocksLocal = 2;
    public static int numberOfBlocksMain = 2;
    public static double wbDelay = 0;
    public static ProtocolEnum protocol = ProtocolEnum.MSI;
    public static String inputSequence = "";
    public static ProtocolInterface useProtocol;

    public static void updateProtocolObject() {
        switch (protocol) {// selects the selected protocol
            case MSI:
                useProtocol = new MSI();
                break;
            case MOSI:
                useProtocol = new MOSI();
                break;
            case MESI:
                useProtocol = new MESI();
                break;
            case MERSI:
                useProtocol = new MERSI();
                break;
            case DRAGON:
                useProtocol = new Dragon();
                break;
            case FIREFLY:
                useProtocol = new Firefly();
                break;
        }
    }
}
