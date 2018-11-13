package com.ehelpy.brihaspati4.authenticate ;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import com.ehelpy.brihaspati4.voip.voip_rxcall;
import com.ehelpy.brihaspati4.indexmanager.IndexManagement;
import com.ehelpy.brihaspati4.comnmgr.CommunicationManager;
import com.ehelpy.brihaspati4.indexmanager.IndexManagementUtilityMethods;
import com.ehelpy.brihaspati4.overlaymgmt.OverlayManagement;
import com.ehelpy.brihaspati4.overlaymgmt.OverlayManagementUtilityMethods;
import com.ehelpy.brihaspati4.routingmgmt.RMThreadPrimary;
import com.ehelpy.brihaspati4.routingmgmt.SysOutCtrl;
import com.ehelpy.brihaspati4.routingmgmt.UpdateIP;

public class ClientMain extends Thread {

//	private static int port = 9999;
//	private static Socket socket = null;
//	public static ServerSocketChannel ss;

    private static X509Certificate client_cert = null;
    private static X509Certificate server_cert = null;
    private static boolean flagset = false;
    public static int CtrlConsoleOut=0;
// Loggin level. 0 allows all comments, 1 basic thread

    public static void main(String args[]) throws Exception {

        GlobalObject global_object = GlobalObject.getGlobalObject();
        global_object.setRunStatus(true);
        // GlobalObject will keep status of various threads and run status. This will be used
        // for proper closure of threads when closing the application.

        com.ehelpy.brihaspati4.routingmgmt.GetProperties.Debug.Properties();
        CtrlConsoleOut=com.ehelpy.brihaspati4.routingmgmt.GetProperties.Property_sysout;

        SysOutCtrl.SysoutSet("iptable initiated"+CommunicationManager.myIpTable);
        UpdateIP IPUpdate = new UpdateIP();

        IPUpdate.start();
        IPUpdate.setName("IPUpdate");
        SysOutCtrl.SysoutSet("Thread Id : "+IPUpdate.getName(), 1);

        @SuppressWarnings("unused")
        Config conf=Config.getConfigObject();

        // Config initialization from configuration file during by the constructor of Config.
        // Config_object will keep the data after reading from configuration file.
        // On each change, the data should be written back to config file also.
        // It implies, in each write api, write to config file on disk is to be implemented.
        boolean timeflg=dateTimeCheck.checkDate();
        // If the time flag returns false then exit the user from the system
        // otherwise start the services.
        if (!timeflg)
        {
            String msg = "Please reset your system time and try again." ;
            Gui.showMessageDialogBox(msg);
            //Exit the system.
            System.exit(0);

        }


        // call boolean dateTimeCheck
        //if false - exit with msg

        //if true - call readVerifyCert

        else {

            try {
                flagset = ReadVerifyCert.verifyCert();
                client_cert = ReadVerifyCert.returnClientCert();
                server_cert = ReadVerifyCert.returnServerCert();
                debug_level.debug(0,"clientcertsaved is =" + client_cert );
                debug_level.debug(0,"servercertsaved is =" + server_cert );
                String email_id=emailid.getemaild();
                debug_level.debug(0,"my email is =" + email_id );

                // The above line is used for debugging.

            } catch (CertificateException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        if(global_object.getRunStatus()) {
            if(flagset) {
                debug_level.debug(0,"The private key of client is  =" + ReadVerifyCert.getKeyPair() );

                // call objects and methods from classes of - routing and overlay mangement
                //OverlayManagementUtilityMethods.fillMyIptable();
                OverlayManagement.nodeStartUp();

//	Generate_newnode.generateNewNodeId(40);


                OverlayManagement olay = new OverlayManagement();
                olay.start();
                Thread.sleep(5000);
                try {
                    olay.join(9000);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }


                // call objects and methods from classes of - routing  mangement
                RMThreadPrimary RM = new RMThreadPrimary();
                RM.start();
                try {
                    RM.join(9000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // call objects and methods from classes of - communication
                CommunicationManager cm= new CommunicationManager();
                cm.start();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //   System.out.println("visibility of fromNodeIdList in P2PMain"+CommunicationManager.fromNodeIdList);
                System.out.println("Starting IndexManagement thread");

                // call objects and methods from classes of - index management
                IndexManagement indmgt= new IndexManagement();
                indmgt.start();
                OverlayManagement.iAmNewlyJoinedNode = true;
                // indmgt.getState();
                // System.out.println("changing iAmNewlyJoinedNode varialbe in Overlay to true");
                // OverlayManagement.iAmNewlyJoinedNode=true;
                //OverlayManagement.flagMyPredecessorsUpdatedForIndexManager=true;

                // call objects and methods from classes of - VOIP Call
                /* 		  ServerSocketChannel ss = ServerSocketChannel.open();
                 		  ss.socket().bind(new InetSocketAddress(port));
                 		  ss.configureBlocking(false);
                 		  System.out.println("flag value is = " + flagset);
                 		  Thread t = new voip_rxcall(ss, port);
                 		  t.start();*/
                b4server_services.service();

                // start user specific services
                // user specific DFS mount service,
                // call objects and methods from classes of - routing and overlay mangement


                // All generic services Interface
                // VOIP call, storage services, messaging service


            }

        }
        else {
            GlobalObject.setRunStatus(true);
            ClientMain.main(args);

        }
    }
}
