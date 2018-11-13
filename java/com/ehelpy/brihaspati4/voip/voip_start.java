package com.ehelpy.brihaspati4.voip;

import java.awt.EventQueue;

import javax.swing.JFrame;

import com.ehelpy.brihaspati4.authenticate.b4server_services;
import com.ehelpy.brihaspati4.authenticate.debug_level;
import javax.crypto.SecretKey;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.Image;
import javax.swing.JLabel;

public class voip_start extends Thread  {

    private JFrame frame;
    private static int port1=8880 ;
    public static Socket socket1 ;
    private static int port2=8880 ;
    public static Socket socket2 ;
    public static ServerSocket serverSocket = null;
    private static voip_key enc_key = null;
    private static SecretKey sec_key = null;
    public static AudioFormat audio1,audio2 ;
    public static DataLine.Info info, info_out ;
    public static  TargetDataLine audio_in;
    public static SourceDataLine audio_out;
    public static boolean calling,flag = false;
    public  static DatagramSocket datagramSocket ;



    /**
     * Launch the application.
     */
    public static void start(String IPaddr, long sym_key) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    voip_start window = new voip_start(IPaddr, sym_key);
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public voip_start(String IPaddr, long sym_key) {
        initialize(IPaddr, sym_key);
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize(String IPaddr, long sym_key) {
        frame = new JFrame();
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);


        try {
            InetAddress.getByName(IPaddr);
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }
        debug_level.debug(0, "The ip address of the far end client has reached the callmanager");

        debug_level.debug(1,"Socket created for txn of data in port number =   " +  port1 + port2);
        enc_key = new voip_key(sym_key);

        Thread t = new Thread(enc_key);
        t.start();
        System.out.println("w1");
        try {
            sec_key = enc_key.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        audio1 = getAudioFormat();
        info = new DataLine.Info(TargetDataLine.class, audio1);
        audio2 = getAudioFormat();
        info_out = new DataLine.Info(SourceDataLine.class, audio2);
        if(!(AudioSystem.isLineSupported(info)||AudioSystem.isLineSupported(info_out))) {
            System.out.println("not supported audio format");
            System.exit(0);
        }

        try {
            audio_in = (TargetDataLine)AudioSystem.getLine(info);
            audio_in.open(audio1);
        } catch (LineUnavailableException e) {
            audio_in.drain();
            audio_in.close();
            audio_nosupport.id_exist();
        }

        audio_in.start();
        try {
            audio_out = (SourceDataLine)AudioSystem.getLine(info_out);
            audio_out.open(audio2);
        } catch (LineUnavailableException e) {
            audio_in.drain();
            audio_in.close();
            e.printStackTrace();
        }

        audio_out.start();
        player_thread p = new player_thread(sec_key);
        player_thread.audio_out = audio_out;
        try {
            player_thread.din = new DatagramSocket(port1);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        calling = true;
        flag = true;

        recorder_thread rec = new recorder_thread(sec_key);
        rec.audio_in = audio_in;
        p.start();

        rec.comn_port = port2;
        rec.start();
        /*if(!rec.isAlive())
        	{
         voip_receive.music();

         }

        if(rec.isAlive()) {
         voip_receive.music_on =false;

        }*/
        JButton btnNewButton = new JButton("Stop");
        btnNewButton.setFont(new Font("Times New Roman", Font.BOLD, 20));
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {

                calling=false;
                flag = false;

                player_thread.din.close();
                debug_level.debug(0,"The client main started");
                b4server_services.service();
                frame.dispose();

            }
        });
        btnNewButton.setBounds(165, 183, 117, 57);
        frame.getContentPane().add(btnNewButton);

        JLabel lblLabel = new JLabel("");
        Image img = new ImageIcon(this.getClass().getResource("/phone_call.png")).getImage();
        lblLabel.setIcon(new ImageIcon(img));
        lblLabel.setBounds(152, 30, 128, 124);
        frame.getContentPane().add(lblLabel);

    }
    private static AudioFormat getAudioFormat() {
        float samplerate = 8000.0F;
        int samplesizebits = 16;
        int channel = 2;
        boolean signed = true;
        boolean bigEndian = false;

        return new AudioFormat(samplerate,samplesizebits,channel,signed,bigEndian );
    }
}