package base;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.text.DefaultCaret;

public class Client extends JFrame{

	static final long serialVersionUID = 1L;
	
	String VERSION = "1.0";
	
	JTextPane chat = new JTextPane();
	JScrollPane scroll = new JScrollPane(chat);
	JTextField champs = new JTextField();
	Socket socket;
	BufferedReader in;
    PrintWriter out;
    ArrayList<String> last = new ArrayList<String>();
    int lastnb = 0;
    Log l;
    Thread t;
    boolean conn = false;
    Timer check;
    
	public static void main(String[] args) {
		new Client();
	}
	
	public Client(){
		makeFenetre();
		l.say("Entrez \"/connect IP\" pour vous connecter");
		check = new Timer(1000,new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(l.conn == false && conn == true){
					deco();
				}
			}
		});
		
		check.start();
	}
	
	void deco(){
		try {
			out.close();
			in.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		l.say("Déconnecté");
		l.conn = false;
		conn = false;
	}
	
	void quitter(){
		deco();
		System.exit(ABORT);
	}
	
	void connexion(InetAddress a) throws IOException{
		l.say("Tentative de connection à "+a+" ...");
		socket = new Socket(a,2000);
		in = new BufferedReader (new InputStreamReader (socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream());
		l.say("Connecté à "+a);
		l.conn = true;
		conn = true;
		Thread t = new Thread(new Reception(in,l));
		t.start();
	}
	
	void makeFenetre(){
		this.setTitle("LanChat - Client (v"+VERSION+")");
		this.setSize(400,400);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		chat.setEditable(false);
		chat.setText("");
		((DefaultCaret)chat.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		scroll.setAutoscrolls(true);
		this.add(scroll,BorderLayout.CENTER);
		this.add(champs,BorderLayout.SOUTH);
		
		champs.addKeyListener(new KeyListener(){
			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent arg0) {}
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()){
					case KeyEvent.VK_ENTER:
						envoyer();
						break;
					case KeyEvent.VK_UP:
						if(lastnb > 0){
							lastnb--;
						}
						champs.setText(last.get(lastnb));
						break;
					case KeyEvent.VK_DOWN:
						if(lastnb < last.size()){
							lastnb++;
						}
						if(lastnb == last.size()){
							champs.setText("");
						}else{
							champs.setText(last.get(lastnb));
						}
						break;
					default:
						lastnb = last==null?0:last.size();
						break;
				}
			}
			
		});
		l = new Log(chat,this);
		l.say("LanChat Client version "+VERSION);
		l.refresh();
		this.setVisible(true);
	}
	
	void envoyer(){
		String msg = champs.getText();
		champs.setText(null);
		last.add(msg);
		lastnb = last.size();
		
		if(conn && msg != null &&  msg != "" && msg.toCharArray().length > 0){
			out.println(msg);
			out.flush();
			if(msg.split(" ")[0].equals("/disconnect"))deco();
		}else if(!conn){
			if(msg.split(" ")[0].equals("/connect") && msg.split(" ").length >= 2){
				try {
					if(msg.split(" ")[1].equalsIgnoreCase("localhost")){
						connexion(InetAddress.getLocalHost());
					}else{
						connexion(InetAddress.getByName(msg.split(" ")[1]));
					}
				} catch (IOException e) {
					l.say("Erreur de connexion");
				}
			}else{
				l.say("Pas connecté, pour vous connecter, tapez : /connect IP");
			}
		}
	}


}

class Reception implements Runnable{

	private char ctemp = 0;
	private Log l;
	private BufferedReader in;
	private boolean stop = false;
	
	public Reception(BufferedReader in,Log l){
		this.in = in;
		this.l = l;
	}
	
	@Override
	public void run() {
		while(!stop){
			try {
				ctemp = (char) in.read();
				if(in.ready()){
					String msg = in.readLine();
					if(msg != null && msg != ""){
						if(ctemp > 0)msg = ctemp+msg;
						l.say(msg);
					}
				}			
			} catch (IOException e2) {
				stop = true;
				l.conn = false;
			}
		}
	}
	
}
