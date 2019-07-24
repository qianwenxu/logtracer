package com.bishe2.bishe2;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis3D;
import org.jfree.chart.axis.NumberAxis3D;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.TextAnchor;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	//我写的
	static int spannum=0;
	static int tracenum=0;
	static trace[] alltrace=new trace[500];
	static int[] clustertracenum;
	static int[] minidseq;
	static ArrayList<ArrayList<trace>> kmeansres1;
	static HashMap<Integer,String> spanidmap=new HashMap<Integer,String>();
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);
		
		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		
		String formattedDate = dateFormat.format(date);
		
		model.addAttribute("serverTime", formattedDate );
		
		return "home";
	}
	
	@SuppressWarnings("null")
	@RequestMapping(value ="search", method = RequestMethod.GET)
	public String search(String searchstr,Model model){
		spannum=0;
		tracenum=0;
		alltrace=new trace[500];
		spanidmap=new HashMap<Integer,String>();
		//上面我加的
		System.out.println("searchstr"+searchstr);
		int k=Integer.parseInt(searchstr);
		kmeansres1=kmeansrun(k);
		model.addAttribute("kmeansres1", kmeansres1);
		model.addAttribute("k", k-1);
		return "kmeansres";
	}
	
	@SuppressWarnings("null")
	@RequestMapping("/detail")
	public String detail(HttpServletRequest request,Model model){
		String clusterNo=request.getParameter("clusterNo");
		System.out.println("clusterNo:"+clusterNo);
		int errornum=0;
		int successnum=0;
		int[] temeveryspanerrornum=new int[spannum];
		int[] temeveryspannum=new int[spannum];
		Arrays.fill(temeveryspanerrornum, 0);
		Arrays.fill(temeveryspannum, 0);
		for(int i=0;i<kmeansres1.get(Integer.parseInt(clusterNo)).size();i++){
			if(kmeansres1.get(Integer.parseInt(clusterNo)).get(i).iserror>0){
				errornum++;
			}else{
				successnum++;
			}
			for(int j=0;j<kmeansres1.get(Integer.parseInt(clusterNo)).get(i).everyspanerrornum.length;j++){
				temeveryspanerrornum[j]+=kmeansres1.get(Integer.parseInt(clusterNo)).get(i).everyspanerrornum[j];
				temeveryspannum[j]+=kmeansres1.get(Integer.parseInt(clusterNo)).get(i).everyspannum[j];
			}
		}
		drawtracepie(errornum,successnum,Integer.parseInt(clusterNo));
		//draw error log
		if(errornum>0){
			drawerrorspanpie(temeveryspanerrornum,temeveryspannum,Integer.parseInt(clusterNo));
		}
		model.addAttribute("errornum", errornum);
		model.addAttribute("cluno", Integer.parseInt(clusterNo));
		return "piechart";
	}
	
	
	@SuppressWarnings("null")
	@RequestMapping("/trace")
	public String trace(HttpServletRequest request,Model model){
		String clusterNo=request.getParameter("clusterNo");
		System.out.println("traceclusterNo:"+clusterNo);
		int cluno=Integer.parseInt(clusterNo);
		findtrace(cluno);
		model.addAttribute("clustertrace", kmeansres1.get(cluno));
		return "tracepic";
	}
	
	
	//工具类，不响应request
	static void drawtracepie(int errornum,int successnum,int cluno) {
		/* 1、创建饼形图数据集对象 DefaultPieDataset */
        DefaultPieDataset dataset = new DefaultPieDataset();
        /* 2、往饼形图数据集对象 DefaultPieDataset 中添加数据 */
        dataset.setValue("报错的trace", errornum);
        dataset.setValue("没报错的trace", successnum);

        /* 3、创建图形对象 JFreeChart：主标题的名称，图标显示的数据集合，是否显示子标题，是否生成提示的标签，是否生成URL链接 */
        String title = "一个聚类里trace报error比重";
        JFreeChart chart = ChartFactory.createPieChart3D(title, dataset, true, true, true);

        /* 4、处理乱码 */
        // 处理主标题的乱码
        chart.getTitle().setFont(new Font("宋体", Font.BOLD, 18));
        // 处理子标题乱码
        chart.getLegend().setItemFont(new Font("宋体", Font.BOLD, 15));
        /* 5、获取饼形图图表区域对象 PiePlot3D */
        PiePlot3D categoryPlot = (PiePlot3D) chart.getPlot();
        /* 6、处理图像上的乱码 */
        categoryPlot.setLabelFont(new Font("宋体", Font.BOLD, 15));
        /* 7、设置图形的生成数据格式为（张三 40 （40%）） */
        String format = "{0} {1} ({2})";
        categoryPlot.setLabelGenerator(new StandardPieSectionLabelGenerator(format));

        /* 8、生成相应的图片 */
        File file = new File("I:/bishe/pic/tracepie"+cluno+".jpg");
        try {
            ChartUtilities.saveChartAsJPEG(file, chart, 800, 600);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
	
	
	static void drawerrorspanpie(int[] temeveryspanerrornum,int[] temeveryspannum,int cluno) {
		getspanidmap();
		/* 1、创建饼形图数据集对象 DefaultPieDataset */
		DefaultCategoryDataset dataset=new DefaultCategoryDataset();
        /* 2、往饼形图数据集对象 DefaultPieDataset 中添加数据 */
        for(int i=0;i<spannum;i++){
        	if(temeveryspanerrornum[i]>0){
        		dataset.addValue(temeveryspanerrornum[i],"error", "id:"+i);
        		dataset.addValue(temeveryspannum[i],"all",  "id:"+i);
        	}
        }

        /* 3、创建图形对象 JFreeChart：主标题的名称，图标显示的数据集合，是否显示子标题，是否生成提示的标签，是否生成URL链接 */
        String title = "每个报错的span的错误日志和这个span的总日志条目统计表";
        JFreeChart chart=ChartFactory.createBarChart3D(
        		title,
                "SpanID",//X轴的标签 
                "日志条数",//Y轴的标签 
                dataset, //图标显示的数据集合
                PlotOrientation.VERTICAL, //图像的显示形式（水平或者垂直）
                true,//是否显示子标题 
                true,//是否生成提示的标签 
                true); //是否生成URL链接      
               //处理图形上的乱码
                //处理主标题的乱码
      //处理图形上的乱码
        //处理主标题的乱码
        chart.getTitle().setFont(new Font("宋体",Font.BOLD,18));
        //处理子标题乱码
        chart.getLegend().setItemFont(new Font("宋体",Font.BOLD,12));
        //获取图表区域对象
        CategoryPlot categoryPlot = (CategoryPlot)chart.getPlot();
        //获取X轴的对象
        CategoryAxis3D categoryAxis3D = (CategoryAxis3D)categoryPlot.getDomainAxis();
        //获取Y轴的对象
        NumberAxis3D numberAxis3D = (NumberAxis3D)categoryPlot.getRangeAxis();
        //处理X轴上的乱码
        categoryAxis3D.setTickLabelFont(new Font("宋体",Font.BOLD,15));
        //处理X轴外的乱码
        categoryAxis3D.setLabelFont(new Font("宋体",Font.BOLD,15));
        //处理Y轴上的乱码
        numberAxis3D.setTickLabelFont(new Font("宋体",Font.BOLD,15));
        //处理Y轴外的乱码
        numberAxis3D.setLabelFont(new Font("宋体",Font.BOLD,15));
        //处理Y轴上显示的刻度，以10作为1格
        numberAxis3D.setAutoTickUnitSelection(false);
        NumberTickUnit unit = new NumberTickUnit(10);
        numberAxis3D.setTickUnit(unit);
        //获取绘图区域对象
        BarRenderer3D barRenderer3D = (BarRenderer3D)categoryPlot.getRenderer();
        //设置柱形图的宽度
        barRenderer3D.setMaximumBarWidth(0.07);
        //在图形上显示数字
        barRenderer3D.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        barRenderer3D.setBaseItemLabelsVisible(true);
        barRenderer3D.setBaseItemLabelFont(new Font("宋体",Font.BOLD,15));
        
        //搭配ItemLabelAnchor TextAnchor 这两项能达到不同的效果，但是 ItemLabelAnchor最好选OUTSIDE，因为INSIDE显示不出来 
        barRenderer3D.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.CENTER_LEFT)); 
        //下面可以进一步调整数值的位置，但是得根据ItemLabelAnchor选择情况，例 如我选的是OUTSIDE12，那么下面设置为正数时，数值就会向上调整，负数则向下 
        barRenderer3D.setItemLabelAnchorOffset(15); 
        //在D盘目录下生成图片
        File file = new File("I:/bishe/pic/errorspanbar"+cluno+".jpg");
        try {
            ChartUtilities.saveChartAsJPEG(file, chart, 800, 600);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	static ArrayList<ArrayList<trace>> kmeansrun(int k){
		ArrayList<ArrayList<trace>> kmeansres=new ArrayList<ArrayList<trace>>();
        try{
            BufferedReader br = new BufferedReader(new FileReader("J:\\java\\trace_log\\traceinfo.txt"));//构造一个BufferedReader类来读取文件
            String s = br.readLine();
            //System.out.println("s:"+s);
            String[] temp=s.split(" ");
            spannum=Integer.parseInt(temp[1]);
            //System.out.println("spannum:"+spannum);
            Integer[] everyspannum1=new Integer[spannum];
            Integer[] everyspanerrornum1=new Integer[spannum];
            while((s = br.readLine())!=null){//使用readLine方法，一次读一行
            	String[] temp1=s.split(" ");
                //System.out.println("s:"+s);
                ArrayList<Integer> span_sequence1 = new ArrayList<Integer>();
                String[] temp11=temp1[1].split(",");
                for(int j=0;j<temp11.length;j++){
                	//System.out.print(temp11[j]+";");
                	span_sequence1.add(Integer.parseInt(temp11[j]));
                }
                //System.out.println();
                String[] temp21=temp1[2].split(",");
                String[] temp31=temp1[3].split(",");
                //System.out.println("temp21"+temp21[1].toString());
                //System.out.println("temp21length:"+temp21.length);
                for(int j=0;j<spannum;j++){
                	//System.out.print(temp21[j]+";");
                	everyspannum1[j]=Integer.parseInt(temp21[j]);
                	everyspanerrornum1[j]=Integer.parseInt(temp31[j]);
                }
                //System.out.println();
                alltrace[tracenum++]=new trace(spannum,temp1[0],span_sequence1,everyspannum1,everyspanerrornum1);
                //alltrace[tracenum-1].print();
            }
            br.close();  
            int intrtime=1000;
            minidseq=new int[tracenum];
            float mindis=99999999;
            while(intrtime-->0){
            	//System.out.println("------------------------");
            	//读取数据完毕，开始初始化clustercenter
                float [][]clustercenter=chooseclustercenter(k);
                //System.out.println("++++++++++++++++++++");
                clusteridseq cis=onetimekmeansrun(clustercenter,k);
                //System.out.println("000000000000000000");
                //cis.print();
                if(mindis>cis.alldis&&cis.alldis!=0){
                	mindis=cis.alldis;
                	minidseq=cis.cluidseq.clone();
                }
            }
            
            System.out.println("最后：");
            System.out.println("mindis:"+mindis);
            clusteridseq mincis=new clusteridseq(mindis,minidseq);
            File writename = new File("J:\\java\\trace_log\\kmeansresult.txt");
			writename.createNewFile();
			BufferedWriter out1 = new BufferedWriter(new FileWriter(writename)); 
			out1.write("");
			out1.flush();
			out1.close();
            mincis.print();
            clustertracenum=new int[k];
            Arrays.fill(clustertracenum, 0);
    		for(int i=0;i<k;i++){
            	System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");
            	System.out.println("clusterid："+i);
    			BufferedWriter out = new BufferedWriter(new FileWriter("J:\\java\\trace_log\\kmeansresult.txt",true)); //文件存在则继续写入否则新建文件再写入
    			out.write("-----------------------------------------------------------------------------------------------------------------------------------------------------"+"\r\n");
        		out.write("clusterid:"+i+"\r\n");
        		out.flush(); 
        		out.close();
        		ArrayList<trace> newclu=new ArrayList<trace>();
            	for(int j=0;j<tracenum;j++){
            		if(minidseq[j]==i){
            			alltrace[j].setclusterid(i);
            			alltrace[j].print();
            			clustertracenum[i]++;
            			newclu.add(alltrace[j]);
            		}
            	}
            	kmeansres.add(newclu);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return kmeansres;
	}
	static void findtrace(int clusterid){
		//GSP
		System.out.println(clusterid);
		System.out.println(clustertracenum[clusterid]);
		System.out.println(clusterid);
		System.out.println(tracenum);
		System.out.println(alltrace);
		System.out.println(minidseq);
    		GSP gsp = new GSP(clustertracenum[clusterid],clusterid,tracenum,alltrace,minidseq);
    		gsp.outputInput();
    		ArrayList<Sequence> result = gsp.getSequences();
	}
	static float [][] chooseclustercenter(int k){
		float [][]clustercenter=new float[k][spannum];
		boolean flag[]=new boolean[tracenum];
		Arrays.fill(flag, false);
		for(int i=0;i<k;i++){
			int j;
			do{
				Random random = new Random();
				//System.out.println("tracenum"+tracenum);
				j=random.nextInt((int)tracenum);
				for(int s=0;s<spannum;s++){
					clustercenter[i][s]=alltrace[j].everyspannum[s];
				}
			}while(flag[j]==true);
			flag[j]=true;
			//System.out.println(i+" ok");
		}
		return clustercenter;
	}
	static clusteridseq onetimekmeansrun(float [][]clustercenter,int k){
		float lastalldis=0;
		float alldis=99999999;
		int[] cluidseq=new int[tracenum];
		int time=0;
		while(Math.abs(lastalldis-alldis)>0|time==1000){
			time++;
			lastalldis=alldis;
			//分配点到类
			alldis=0;
			float [][]newclustercenter=new float[k][spannum];
			for(int s=0;s<k;s++){
				Arrays.fill(newclustercenter[s],0);
			}
			int []newclusternum=new int[k];
			Arrays.fill(newclusternum,0);
			for(int i=0;i<tracenum;i++){
				float mindis=99999999;
				int minid=0;
				for(int j=0;j<k;j++){
					float dis=distance(alltrace[i],clustercenter[j]);
					if(dis<mindis){
						minid=j;			
						mindis=dis;
					}
				}
				alltrace[i].clusterid=minid;
				newclusternum[minid]++;
				for(int s=0;s<spannum;s++){
					newclustercenter[minid][s]+=alltrace[i].everyspannum[s];
				}
				alldis+=mindis;
				cluidseq[i]=minid;
			}
			//更新中心
			for(int i=0;i<k;i++){
				for(int s=0;s<spannum;s++){
					if(Math.abs(newclusternum[i]-0)==0){
						newclustercenter[i][s]=0;
					}else{
						newclustercenter[i][s]/=newclusternum[i];
					}
				}
			}
			clustercenter=newclustercenter.clone();
			//for(int i=0;i<k;i++){
				//System.out.print("clustercenter["+i+"][0]:");
				//for(int j=0;j<spannum;j++){
					//System.out.print(newclustercenter[i][j]+",");
				//}
				//System.out.println();
			//}
		}
		return new clusteridseq(alldis,cluidseq);
	}
	static float distance(trace tr,float[] clustercenter){
		float dis=0;
		float AiBisum=0,Ai2sum=0,Bi2sum=0;
		for(int i=0;i<spannum;i++){
			//dis+= Math.pow((tr.everyspannum[i]-clustercenter[i]),2);
			AiBisum+=tr.everyspannum[i]*clustercenter[i];
			Ai2sum+=tr.everyspannum[i]*tr.everyspannum[i];
			Bi2sum+=clustercenter[i]*clustercenter[i];
		}
		int missedspan=0;
		for(int i=0;i<spannum;i++){
			if((tr.everyspannum[i]==0&&clustercenter[i]!=0)||(tr.everyspannum[i]!=0&&clustercenter[i]==0)){
				missedspan++;
			}
		}
		dis=(float)((1-(float)(AiBisum/(Math.sqrt(Ai2sum)*Math.sqrt(Bi2sum))))*0.1+((float)missedspan/(float)spannum)*0.9);
		//dis=((float)missedspan/(float)spannum);
		return dis;
	}
	
	static void getspanidmap(){
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("J:\\java\\trace_log\\spanid.txt"));
			String s;
			int id=0;
	        while((s = br.readLine())!=null){//使用readLine方法，一次读一行
	        	String[] temp1=s.split(" ");
	            //System.out.println(id+++":"+temp1[0]+temp1[1]);
	            spanidmap.put(id++, temp1[0]+temp1[1]);
	        }
	        br.close();  
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//构造一个BufferedReader类来读取文件
	}
}

//其它的自定义类

class clusteridseq{
	static float alldis;
	static int[] cluidseq;
	clusteridseq(float alldis1,int[] cluidseq1){
		alldis=alldis1;
		cluidseq=cluidseq1.clone();
	}
	clusteridseq(float alldis1){
		alldis=alldis1;
	}
	static void print(){
		
		System.out.println(Arrays.toString(cluidseq));
		System.out.println("alldis:"+alldis);
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("J:\\java\\trace_log\\kmeansresult.txt",true)); //文件存在则继续写入否则新建文件再写入
			out.write(Arrays.toString(cluidseq)+"\r\n");
			out.write("alldis:"+alldis+"\r\n");
			out.flush(); 
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//System.out.println(Arrays.toString(cluidseq));
		//System.out.println("alldis:"+alldis);
	}
}

/**
 * <title>GSP算法实现类</title>
 * 本类为核心类，在本类中实现了GSP算法
 * @author Sunny
 *
 */
class GSP {
    private ArrayList<Sequence> c; //长度为i的候选序列模式
    private ArrayList<Sequence> l; //长度为i的序列模式
    private ArrayList<Sequence> l1; //L(1)
    private ArrayList<Integer>[] resseq; 
    private ArrayList<Sequence> result;
    private SeqDB db;
    private int support;
    int time=0;//将l加入result的次数
    static HashMap<Integer,String> spanidmap=new HashMap<Integer,String>();
    
   /**
    * 构造方法
    * 在实例化GSP对象时，同时赋值支持度
    * 并获取序列集和初始化序列模式结果
    * @param support 支持度
    */
    public GSP(int support,int clusterid,int tracenum,trace[] alltrace,int[] minidseq) {
        this.support = support;                   //赋值支持度
        this.db = new SeqDB(clusterid,tracenum,alltrace,minidseq);                    //从SeqDB类中获取设置好的序列集
        this.result = new ArrayList<Sequence>();  //初始化序列模式结果对象
    }
    
    /**
     * 产生序列模式
     * 核心方法，在该方法中调用连接和剪枝操作，并将最后获得的序列模式放到result中
     * @return 序列模式
     */
    public ArrayList getSequences() {
        long start = System.currentTimeMillis();
        //调用初始化方法
        initialize();
        System.out.println("序列模式L(1) 为：" +l);
        l1=(ArrayList<Sequence>) l.clone();
        resseq=new ArrayList[l1.size()];
        for(int i=0;i<l1.size();i++){
        	resseq[i]=new ArrayList<Integer>();
        }
        System.out.println(".................................................");
        for (int i = 0; i < l.size(); i++) {
        	//产生进行连接操作后的候选集
        	genCandidate();      
            if (!(c.size() > 0)) {
                break;
            }         
            System.out.println("剪枝前候选集的大小为："+c.size()+" 候选集c为："+c);
            //进行剪枝操作
            pruneC();
            System.out.println("剪枝后候选集的大小为："+c.size()+" 候选集c为："+c);
            //产生序列模式
            generateL();
            System.out.println("序列模式L(" + (i + 2) + ") 为：" +l);
            addToResult(l);
            System.out.println(".................................................");
        }
        System.out.println("序列模式L(1) 为：" +l1);
        long end = System.currentTimeMillis();
        System.out.println("计算花费时间" + (end - start) + "毫秒!");
        importantseq();
        return this.result;
    }
    /*
     * 初始化方法
     * 获取设置好的序列集
     */
    private void initialize() {
        Map<Integer, Integer> can = new HashMap<Integer, Integer>();
        //对于序列集中的所有序列
        for (Sequence s : db.getSeqs()) {
        	//对于序列中的所有项目集
            for (Element e : s.getElements()) {
            	//对于项目集中的所有项目
                for (int i : e.getItems()) {
                	//比较项目的出现次数，并计数，用于与支持度比较
                    if (can.containsKey(i)) {
                        int count = can.get(i).intValue() + 1;
                        can.put(i, count);
                    } else {
                        can.put(i, 1);
                    }
                }
            }
        }
        this.l = new ArrayList<Sequence>();
        //对于产生的候选集，如果支持度大于最小支持度阈值，则添加到序列模式L中
        for (int i : can.keySet()) {
            if (can.get(i).intValue() >= support) {
                Element e = new Element(new int[] {i});
                Sequence seq = new Sequence();
                seq.addElement(e);
                this.l.add(seq);
            }
        }
        //将第一次频繁序列模式加入结果集中
        this.addToResult(l);
       
    }
    
    /*
     * 产生经过连接操作后的候选集
     * 
     */
    private void genCandidate() {
        this.c = new ArrayList<Sequence>();
        //对于种子集L进行连接操作
        for (int i = 0; i < this.l.size(); i++) {
            for (int j = i; j < this.l.size(); j++) {
                this.joinAndInsert(l.get(i), l.get(j));
                if (i != j) {
                    this.joinAndInsert(l.get(j), l.get(i));
                }
            }
        }
    }

   /*
    * 对种子集进行连接操作
    */
    private void joinAndInsert(Sequence s1, Sequence s2) {
        Sequence s, st;
        //去除第一个元素
        Element ef = s1.getElement(0).getWithoutFistItem(); 
        //去除最后一个元素
        Element ee = s2.getElement(s2.size() - 1).getWithoutLastItem();
        int i = 0, j = 0;
        if (ef.size() == 0) {
            i++;
        }
        for (; i < s1.size() && j < s2.size(); i++, j++) {
            Element e1, e2;

            if (i == 0) {
                e1 = ef;
            } else {
                e1 = s1.getElement(i);
            }
            if (j == s2.size() - 1) {
                e2 = ee;
            } else {
                e2 = s2.getElement(j);
            }
            if (!e1.equalsTo(e2)) {
                return;
            }
        } //end of for

        s = new Sequence(s1);
        //将s2的最后一个元素添加到s中
        (s.getElement(s.size() - 1)).addItem(s2.getElement(s2.size() - 1).
                                            getLastItem());
        //如果候选集中没有s，则添加到候选集
        if (s.notInSeqs(c)) {
            c.add(s);
        }
        st = new Sequence(s1);
        //将s2的最后一个元素添加到st中
        st.addElement(new Element(new int[] {s2.getElement(s2.size() - 1).
                                  getLastItem()}));
        //如果候选集中没有st，则添加到候选集
        if (st.notInSeqs(c)) {
            c.add(st);
        }
        return;
    }

    /*
     * 剪枝操作
     * 看每个候选序列的连续子序列是不是频繁序列
     * 采用逐个取元素，只去其中一个项目，然后看是不是有相应的频繁序列在l中。
     * 如果元素只有一个项目，则去除该元素做相应判断。
     */
    private void pruneC() {
        Sequence s;
        //对于序列中的所有元素
        for (int i = 0; i < this.c.size();i++) {
            s = c.get(i);
            //对于元素中的所有项目
            for (int j = 0; j < s.size(); j++) {
                Element ce = s.getElement(j);
                boolean prune=false;
                //只有一个元素的情况
                if (ce.size() == 1) {
                    s.removeElement(j);
                    //如果子序列不是序列模式，则将它从候选序列模式中删除，否则添加
                    if (s.notInSeqs(this.l)) {
                        prune=true;
                    }
                    s.insertElement(j, ce);
                } else {
                    for (int k = 0; k < ce.size(); k++) {
                        int item=ce.removeItem(k);
                        //如果子序列不是序列模式，则将它从候选序列模式中删除。否则添加
                        if (s.notInSeqs(this.l)) {
                            prune=true;
                        }
                        ce.addItem(item);
                    }
                }
                //如果剪枝，则将该序列删除
                if(prune){
                    c.remove(i);
                    i--;
                    break;
                }
            }
        } 
    }
    
    /*
     * 生成序列模式L
     * 用于已经经过连接和剪枝操作后的后选序列集
     */
    private void generateL() {
        this.l = new ArrayList<Sequence>();
        for (Sequence s : db.getSeqs()) {
            for (Sequence seq : this.c) {
                if (seq.isSubsequenceOf(s)) {
                	//支持度计数
                    seq.incrementSupport();
                }
            }
        }
        for (Sequence seq : this.c) {
        	//大于最小支持度阈值的放到序列模式中
            if (seq.getSupport() >= this.support) {
                this.l.add(seq);
            }
        }
    }
    
    /*
     * 将该频繁序列模式加入结果中
     */
    private void addToResult(ArrayList<Sequence> l) { 
    	if(time!=0){
    		ArrayList<Integer>[] newresseq=new ArrayList[l1.size()]; 
    		for(int i=0;i<l1.size();i++){
    			newresseq[i]=new ArrayList<Integer>();
    		}
            for (int i = 0; i < l.size(); i++) {
            	for(int j=0;j<l1.size();j++){       		
                	if(l1.get(j).isSubsequenceOf(l.get(i))){
                		newresseq[j].add(this.result.size());
                	}
                }
                this.result.add(l.get(i));
            }
            for(int i=0;i<l1.size();i++){
            	if(newresseq[i].size()!=0){
            		resseq[i]=newresseq[i];
            	}
            }
    	}else{
    		for (int i = 0; i < l.size(); i++) {
                this.result.add(l.get(i));
            }
    	}
    	time++;
    }
    
    /**
     * 输出输入的序列信息
     * 输出需要进行序列模式分析的序列以及最小支持度（计数）
     */
   public void outputInput() {
	   System.out.println("最小支持度计数为：" + this.support);
	   System.out.println("输入的序列集合为：");
	   System.out.println(db.getSeqs());
	   System.out.println();
   }
   
   /**
    * 自己写的，提取出有用的序列
    */
   public void importantseq() {
	   getspanidmap();
	   //画图
	   BufferedImage image = new BufferedImage(1500, 500, BufferedImage.TYPE_INT_RGB);
	   Graphics graphics = image.getGraphics();
	   graphics.setColor(new Color(255, 255, 255));
	   graphics.fillRect(0, 0, 1500, 500);
	   int spanWidth=120,spanHeight=80;
	   System.out.println("提取出有用的序列：");
	   ArrayList<Integer> importantseqid=new ArrayList<Integer>();
	   ArrayList<Sequence> updateimpseq=new ArrayList<Sequence>();
	   ArrayList<Integer> drawpointarr=new ArrayList<Integer>();
	   HashMap<Integer,drawPoint> pmap=new HashMap<Integer,drawPoint>();
	   for(int i=0;i<l1.size();i++){
		   for(int j=0;j<resseq[i].size();j++){
			   if(!importantseqid.contains(resseq[i].get(j))){
				   importantseqid.add(resseq[i].get(j));
				   System.out.println(result.get(resseq[i].get(j)));
				   updateimpseq.add(result.get(resseq[i].get(j)));
			   }
		   }
	   }
	   int flag[][]=new int[20][10];
	   for(int i=0;i<20;i++){
		   Arrays.fill(flag[i], 0);
	   }
	   for(int i=0;i<updateimpseq.size();i++){
		   int x=-260,y=0;
		   int x1=0,x2=0,y1=0,y2=0;
		   for(int j=0;j<updateimpseq.get(i).size();j++){
			   if(!pmap.containsKey(updateimpseq.get(i).getElement(j).getItems().get(0))){
				   x=x+300;
				   drawpointarr.add(updateimpseq.get(i).getElement(j).getItems().get(0));
				   if(flag[x/300][y/150]==0){
					   pmap.put(updateimpseq.get(i).getElement(j).getItems().get(0), new drawPoint(x,y));
					   flag[x/300][y/150]=1;
				   }
				   else{
					   pmap.put(updateimpseq.get(i).getElement(j).getItems().get(0), new drawPoint(x,y+150));
					   flag[x/300][y/150+1]=1;
					   y+=150;
				   }
			   }else{
				   x=pmap.get(updateimpseq.get(i).getElement(j).getItems().get(0)).x;
				   y=pmap.get(updateimpseq.get(i).getElement(j).getItems().get(0)).y;
			   }
			   x1=x2;
			   y1=y2;
			   x2=x;
			   y2=y;
			   //画直线
			   if(j!=0){
				   drawline(x1,y1+spanWidth,x2,y2+spanWidth,spanWidth,spanHeight,graphics);
			   }
		   }
	   }
	   for(int i=0;i<pmap.size();i++){
		   System.out.println("val:"+drawpointarr.get(i)+" x:"+pmap.get(drawpointarr.get(i)).x+" y:"+pmap.get(drawpointarr.get(i)).y);
		   drawspan(drawpointarr.get(i),pmap.get(drawpointarr.get(i)).x,pmap.get(drawpointarr.get(i)).y+spanWidth,spanWidth,spanHeight,graphics);
	   }
	   try {
			ImageIO.write(image, "PNG", new File("I:/bishe/pic/trace.jpg"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		graphics.dispose();
	   //System.out.println("输入的序列集合为：");
	   //System.out.println(db.getSeqs());
	   //System.out.println();
   }
   
   public static void drawspan(Integer val,int x,int y,int spanWidth,int spanHeight,Graphics graphics){
   	int fontSize = 22;
   	Font font = new Font("Serief", Font.BOLD, fontSize);
   	graphics.setFont(font);
   	graphics.setColor(new Color(60, 179, 113));
   	graphics.fillRect(x, y, spanWidth, spanHeight);
   	graphics.setColor(new Color(255, 255, 255));
   	int strWidth = graphics.getFontMetrics().stringWidth(spanidmap.get(val));
   	//graphics.drawString(spanidmap.get(val), x+fontSize - (strWidth / 2), y+fontSize);
   	drawStringWithFontStyleLineFeed(graphics, spanidmap.get(val), x+18, y+45, font);
   }
   public static void drawline(int x1,int y1,int x2,int y2,int spanWidth,int spanHeight,Graphics graphics){
	   	graphics.setColor(new Color(0, 0, 0));
	   	//graphics.drawLine(x1+spanWidth, y1+spanHeight/2, x2, y2+spanHeight/2);
	   	drawAL(x1+spanWidth, y1+spanHeight/2, x2, y2+spanHeight/2,graphics);
	}
   public static void drawAL(int sx, int sy, int ex, int ey, Graphics g2)
	{

		double H = 10; // 箭头高度
		double L = 4; // 底边的一半
		int x3 = 0;
		int y3 = 0;
		int x4 = 0;
		int y4 = 0;
		double awrad = Math.atan(L / H); // 箭头角度
		double arraow_len = Math.sqrt(L * L + H * H); // 箭头的长度
		double[] arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
		double[] arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
		double x_3 = ex - arrXY_1[0]; // (x3,y3)是第一端点
		double y_3 = ey - arrXY_1[1];
		double x_4 = ex - arrXY_2[0]; // (x4,y4)是第二端点
		double y_4 = ey - arrXY_2[1];

		Double X3 = new Double(x_3);
		x3 = X3.intValue();
		Double Y3 = new Double(y_3);
		y3 = Y3.intValue();
		Double X4 = new Double(x_4);
		x4 = X4.intValue();
		Double Y4 = new Double(y_4);
		y4 = Y4.intValue();
		// 画线
		g2.drawLine(sx, sy, ex, ey);
		//
		Polygon triangle = new Polygon();
		triangle.addPoint(ex, ey);
		triangle.addPoint(x3, y3);
		triangle.addPoint(x4, y4);
		g2.drawPolygon(triangle);
		//实心箭头
		//g2.fill(triangle);
		g2.fillPolygon(triangle);
		//非实心箭头
		//g2.draw(triangle);

	}
   
// 计算
	public static double[] rotateVec(int px, int py, double ang,
			boolean isChLen, double newLen) {

		double mathstr[] = new double[2];
		// 矢量旋转函数，参数含义分别是x分量、y分量、旋转角、是否改变长度、新长度
		double vx = px * Math.cos(ang) - py * Math.sin(ang);
		double vy = px * Math.sin(ang) + py * Math.cos(ang);
		if (isChLen) {
			double d = Math.sqrt(vx * vx + vy * vy);
			vx = vx / d * newLen;
			vy = vy / d * newLen;
			mathstr[0] = vx;
			mathstr[1] = vy;
		}
		return mathstr;
	}
   private static int  getStringLength(Graphics g,String str) {
       char[]  strcha=str.toCharArray();
       int strWidth = g.getFontMetrics().charsWidth(strcha, 0, str.length());
       System.out.println("字符总宽度:"+strWidth);
       return strWidth;
   }
   private static int getRowStrNum(int strnum,int rowWidth,int strWidth){
   	int rowstrnum=0;
   	rowstrnum=(rowWidth*strnum)/strWidth;
   	System.out.println("每行的字符数:"+rowstrnum);
   	return rowstrnum;
   }
   private static  int  getRows(int strWidth,int rowWidth){
       int rows=0;
       if(strWidth%rowWidth>0){
           rows=strWidth/rowWidth+1;
       }else{
           rows=strWidth/rowWidth;
       }
       System.out.println("行数:"+rows);
       return rows;
   }
   private static int  getStringHeight(Graphics g) {
       int height = g.getFontMetrics().getHeight();
       System.out.println("字符高度:"+height);
       return height;
   }
   private static  void  drawStringWithFontStyleLineFeed(Graphics g, String strContent, int loc_X, int loc_Y, Font font){
       g.setFont(font);
       //获取字符串 字符的总宽度
       int strWidth =getStringLength(g,strContent);
       //每一行字符串宽度
       int rowWidth=190;
       System.out.println("每行字符宽度:"+rowWidth);
       //获取字符高度
       int strHeight=getStringHeight(g);
       //字符串总个数
       System.out.println("字符串总个数:"+strContent.length());
       if(strWidth>rowWidth){
           int rowstrnum=getRowStrNum(strContent.length(),rowWidth,strWidth);
           int  rows= getRows(strWidth,rowWidth);
           String temp="";
           for (int i = 0; i < rows; i++) {
               //获取各行的String 
               if(i==rows-1){
                   //最后一行
                   temp=strContent.substring(i*rowstrnum,strContent.length());
               }else{
                   temp=strContent.substring(i*rowstrnum,i*rowstrnum+rowstrnum);
               }
               if(i>0){
                   //第一行不需要增加字符高度，以后的每一行在换行的时候都需要增加字符高度
                   loc_Y=loc_Y+strHeight;
               }
               g.drawString(temp, loc_X, loc_Y);
           }
       }else{
           //直接绘制
           g.drawString(strContent, loc_X, loc_Y);
       }
       
   }
   
   static void getspanidmap(){
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("J:\\java\\trace_log\\spanid.txt"));
			String s;
			int id=0;
	        while((s = br.readLine())!=null){//使用readLine方法，一次读一行
	        	String[] temp1=s.split(" ");
	            //System.out.println(id+++":"+temp1[0]+temp1[1]);
	            spanidmap.put(id++, temp1[0]+temp1[1]);
	        }
	        br.close();  
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//构造一个BufferedReader类来读取文件
	}
}


/**
 * <title>序列类</title>
 * 序列信息和操作类，在本类中进行序列比较、支持度计数
 * 以及操作该序列的元素（项目集）
 * @author Sunny
 *
 */
class Sequence {
    private int support; //该序列在数据库中的支持计数
    private ArrayList<Element> sequence; //存放元素序列
    
    /**
     * 不带参数的构造方法
     * 实例化Sequence对象时，同时初始化该对象的支持计数和sequence属性
     *
     */
    public Sequence() {
        this.sequence = new ArrayList<Element>();
        this.support = 0;
    }
    
    /**
     * 带参数的构造方法
     * 拷贝参数序列对象s中的所有元素到本对象的sequence属性中
     * 并初始化支持计数
     * @param s Sequence对象
     */
    public Sequence(Sequence s) {
        this.sequence = new ArrayList<Element>();
        this.support = 0;
        //拷贝s中的所有元素
        for (int i = 0; i < s.size(); i++) {
            this.sequence.add(s.getElement(i).clone());
        }
    }
    
    /**
     * 添加新的元素
     * 用于向序列中添加新的元素
     * @param e  Element e -- 被添加的元素
     */
    public void addElement(Element e) {
        this.sequence.add(e);
    }
    
    /**
     * 插入元素
     * 向序列中位置index插入新的元素
     * @param index 需要插入的元素位置
     * @param e Element e -- 被插入的元素
     */
    public void insertElement(int index,Element e){
        this.sequence.add(index,e);
    }
    
    /**
     * 删除元素
     * 删除位置index上的元素
     * @param index 位置序号
     * @return 返回删除后的sequence
     */
    public Element removeElement(int index){
        if(index<this.sequence.size()){
            return this.sequence.remove(index);
        }else 
            return null;
    }
    
    /**
     * 获取序列的元素
     * 获取第index个元素
     * @param index 元素在序列中的位置
     * @return 返回该元素
     */
    public Element getElement(int index) {
        if (index >= 0 && index < this.sequence.size()) {
            return this.sequence.get(index);
        } else {
            System.err.println("index outof bound in Seuqence.getElement()");
            return null;
        }
    }
    
    /**
     * 获取所有元素
     * 返回序列对象的sequence属性，也就是所有元素的集合
     * @return  ArrayList -- 所有元素放到ArrayList中
     */
    public ArrayList<Element> getElements() {
        return this.sequence;
    }
    
    /**
     * 获取序列大小
     * @return 序列大小
     */
    public int size() {
        return this.sequence.size();
    }

   /**
    * 比较序列间的元素
    * 将传递的参数序列对象与本序列对象比较
    * 看是否有相同的元素
    * @param seqs 被比较的序列
    * @return true--存在相同元素 false--不存在相同元素
    */ 
    public boolean notInSeqs(ArrayList<Sequence> seqs) {
        Sequence s;
        for (int i=0;i<seqs.size();i++) {
            s=seqs.get(i);
            //调用方法isSubsequenceOf()比较
            if (this.isSubsequenceOf(s) && s.isSubsequenceOf(this)){
                return false;
            }

        }
        return true;
    }
    
    /*
     * 比较序列中是否含有相同的元素
     */
    public boolean isSubsequenceOf(Sequence s) {
        int i = 0, j = 0;
        while (j < s.size() && i < this.sequence.size()) {
            if (this.getElement(i).isContainIn(s.getElement(j))) {
                i++;
                j++;
                if (i == this.sequence.size()) {
                    return true;
                }

            } else {
                j++;
            }
        }
        return false;
    }
    
    /**
     * 增加支持计数
     *
     */
    public void incrementSupport() {
        this.support++;
    }
    
    /**
     * 获取支持计数
     * @return
     */
    public int getSupport() {
        return this.support;
    }
    
    /**
     * 重写toString()
     * 用于输出时的字符处理
     * 
     */
    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("<");
        for (int i = 0; i < this.sequence.size(); i++) {
            s.append(this.sequence.get(i));
            if (i != this.sequence.size() - 1) {
                s.append(" ");
            }
        }
        s.append(">");
        return s.toString();
    }
}


/**
 * <title>数据库序列集类</title>
 * 用于获取从数据库中扫描后获得序列集
 * 这里并没有从数据库获取，而是做好的初始化
 * @author Sunny
 *
 */
class SeqDB {
    private ArrayList<Sequence> seqs;  //序列对象
    
    /**
     * 无参数构造方法
     * 初始化序列集
     *
     */
    public SeqDB(int clusterid,int tracenum,trace[] alltrace,int[] minidseq) {
    	this.seqs = new ArrayList<Sequence>();
        Sequence s;
        for(int j=0;j<tracenum;j++){
      		if(minidseq[j]==clusterid){
                s = new Sequence();
                for(int r=0;r<alltrace[j].span_sequence.size();r++){
                	s.addElement(new Element(new int[] {alltrace[j].span_sequence.get(r)}));
                }
                seqs.add(s);
        	}
       	}
    }
    
    /**
     * 获取序列集的大小即获取有几个序列
     * @return序列集大小
     */
    public int size(){
        return this.seqs.size();
    }
    
    /**
     * 获取序列集
     * @return序列集
     */
    public ArrayList<Sequence> getSeqs(){
        return this.seqs;
    }
}


/**
 * <title>元素（项目集）类</title>
 * 元素信息和操作类，将项目集作为该实例的属性
 * 本类封装了对于项目集的基本操作
 * @author Sunny
 *
 */
class Element {
    private ArrayList<Integer> itemset;//表示该元素的项目，按数字的升序存放
    
    /**
     * 无参数构造方法
     * 初始化项目集
     *
     */
    public Element() {
        this.itemset=new ArrayList<Integer>();
    }
    
    /**
     * 带参数构造方法
     * 初始化项目集，即将参数中的项目集拷贝过来
     * @param items 项目集
     */
    public Element(int [] items){
        this.itemset=new ArrayList<Integer>();
        for(int i=0;i<items.length;i++){
            this.addItem(items[i]);
        }
    }
    
    /**
     * 添加项目
     * 添加项目到项目集中
     * @param item 被添加的项目
     */
    public void addItem(int item){
        int i;
        for(i=0;i<itemset.size();i++){
            if(item<itemset.get(i)){
                break;
            }
        }
        itemset.add(i,item);
    }
    
    /**
     * 获得项目集
     * @return 项目集
     */
    public ArrayList<Integer> getItems(){
        return this.itemset;
    }
    
    /**
     * 获取最后一个位置的项目
     * @return 项目
     */
    public int getLastItem(){
        if(this.itemset.size()>0){
            return itemset.get(itemset.size() - 1);
        }
        else{
            System.err.println("空元素错误，Element.getLastItem()");
            return 0;
        }
    }

    /**
     * 本方法判断本元素是不是包含于元素e中
     * @param e 元素
     * @return true--是 false--否
     */
    public boolean isContainIn(Element e){

        if(this.itemset.size()>e.itemset.size()){//如果两个元素大小不同，则为不相等
            return false;
        }
        int i=0,j=0;
        while(j<e.size() && i<this.itemset.size() ){
            if(this.itemset.get(i).intValue() == e.itemset.get(j).intValue()){
                i++;j++;
            }else{
                j++;			
            }
        }
        if(i==this.itemset.size()){
            return true;
        }else{
            return false;
        }
    }
    
    /**
     * 获取去除第一个项目外的元素
     * @return 元素
     */
    public Element getWithoutFistItem(){
        Element e=new Element();
        for(int i=1 ;i<this.itemset.size();i++){
            e.addItem(this.itemset.get(i).intValue());
        }
        return e;
    }
    
    /**
     * 获取去除最后一个项目外的元素
     * @return 元素
     */
    public Element getWithoutLastItem(){
        Element e=new Element();
        for(int i=0 ;i<this.itemset.size()-1;i++){
            e.addItem(this.itemset.get(i).intValue());
        }
        return e;
    }
    
    /**
     * 删除项目
     * 项目位置i上的项目
     * @param i 位置序号
     * @return         
     */
    public int removeItem(int i){
        if(i<this.itemset.size()){
           return this.itemset.remove(i).intValue();
        }
        System.err.println("无效的索引！");
        return -1;
    }
    
    /**
     * 比较两个元素的大小
     * 将传递过来的参数o与本对象比较
     * @param o 被比较的元素
     * @return int -- -1 本元素小于参数  1 本元素大于参速
     */
     public int compareTo(Object o){
         Element e=(Element)o;
         int r=0;
         int i=0,j=0;
         while(i<this.itemset.size() && j<e.itemset.size()){
            if(this.itemset.get(i).intValue() < e.itemset.get(j).intValue()){
                r=-1;//本element小于e
                break;
            }else{
                if(this.itemset.get(i).intValue() > e.itemset.get(j).intValue()){
                    r=1;//本element大于e
                    break;
                }
            }
            i++;j++;//项目相同，都指向下一个项目
         }
         if(r==0){//如果目前还没有比较出谁大谁小的话
             if(this.itemset.size()>e.itemset.size()){
                 r=1;
             }
             if(this.itemset.size()<e.itemset.size()){
                 r=-1;
             }
         }
         return r;
    }
     
    /**
     * 获取项目集的大小
     * @return int--大小
     */
    public int size(){
        return this.itemset.size();
    }
    
    /**
     * 元素拷贝方法
     * 拷贝项目集
     */
    public Element clone(){
        Element clone=new Element();
        for(int i:this.itemset){
            clone.addItem(i);
        }
        return clone;
    }
    
    /**
     * 下判断两个元素是否相同
     * @param o           
     * @return  true--相同 false--不同
     */
    public boolean equalsTo(Object o){
       boolean equal=true;
       Element e=(Element)o;
       if(this.itemset.size()!=e.itemset.size()){//如果两个元素大小不同，则为不相等
           equal=false;
       }
       for(int i=0;equal && i<this.itemset.size();i++){
           if(this.itemset.get(i).intValue()!=e.itemset.get(i).intValue()){
               equal=false;
           }
       }
       return equal;
   }

    /**
     * 重写toString()
     * 用于输出时的字符处理
     */
    public String toString(){
        StringBuffer s=new StringBuffer();
        if(this.itemset.size()>1){
            s.append("(");
        }
        for(int i=0;i<this.itemset.size();i++){
            s.append(this.itemset.get(i).intValue());
            if(i<this.itemset.size()-1){
                s.append(",");
            }
        }
        if(this.itemset.size()>1){
            s.append(")");
        }
        return s.toString();
    }
}


class drawPoint{
	String spanid;
	public int x;
	public int y;
	drawPoint(int x1,int y1){
		x=x1;
		y=y1;
	}
}
