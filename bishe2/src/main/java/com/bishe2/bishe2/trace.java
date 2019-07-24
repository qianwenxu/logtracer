package com.bishe2.bishe2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class trace {
	int allspannum;
	String traceid;
	ArrayList<Integer> span_sequence = new ArrayList<Integer>();
	Integer[] everyspannum;
	Integer[] everyspanerrornum;
	int iserror=0;
	int clusterid=-1;
	public trace(int allspannum1,String traceid1,ArrayList<Integer> span_sequence1,Integer[] everyspannum1,Integer[] everyspanerrornum1){
		this.allspannum=allspannum1;
		this.everyspannum=everyspannum1.clone();//深拷贝
		this.everyspanerrornum=everyspanerrornum1.clone();
		this.traceid=traceid1;
		span_sequence=span_sequence1;
		for(int i=0;i<everyspanerrornum.length;i++){
			iserror+=everyspanerrornum[i];
		}
	}
	public void setclusterid(int id){
		clusterid=id;
	}
	public void print(){
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("J:\\java\\trace_log\\kmeansresult.txt",true)); //文件存在则继续写入否则新建文件再写入
			out.write("traceid:"+traceid+"\r\n");
			out.write("everyspannum:"+Arrays.toString(this.everyspannum)+"\r\n");
			out.write("everyspanerrornum:"+Arrays.toString(this.everyspanerrornum)+"\r\n");
			out.write("span_sequence:"+span_sequence+"\r\n");
			out.write("iserror:"+iserror+"\r\n");
			out.flush(); 
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("traceid:"+traceid);
		System.out.print("everyspannum:");
		System.out.println(Arrays.toString(this.everyspannum));
		System.out.print("everyspanerrornum:");
		System.out.println(Arrays.toString(this.everyspanerrornum));
		System.out.print("span_sequence:");
		System.out.println(span_sequence);
		System.out.println("iserror:"+iserror);
		System.out.println();
	}
	public String gettraceid(){
		return traceid;
	}
	public int getiserror(){
		return iserror;
	}
	public String getspanseq(){
		return span_sequence.toString();
	}
}
