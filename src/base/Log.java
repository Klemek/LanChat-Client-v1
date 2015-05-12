package base;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JTextPane;

public class Log {

	JTextPane chat;
	ArrayList<String> log = new ArrayList<String>();
	boolean conn = false;
	JFrame f;
	
	public Log(JTextPane chat,JFrame f){
		this.chat = chat;
		this.f= f;
	}
	
	public void say(String msg){
		log.add(msg);
		refresh();
		f.toFront();
	}
	
	public void refresh(){
		if(log != null){
			String text = "";
			for(String s:log){
				text = text.equals("")?s:text+"\n"+s;
			}
			chat.setText(text);
		}
	}
	
}
