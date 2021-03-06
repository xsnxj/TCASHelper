
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * TCASHelp(The comprehensive assessment system Helper)
 * 综合测评系统助手
 * Created by Ericwyn on 2017/2/16.
 */


public class MainActivity {

    private static String EXCEL_PATH;
    private static String WT_PATH;
    private static String OUTPUT_PATH;
    private static List<Map<String ,Object>> list_ofData;
    private static HashMap<String,Object> marksMap;
    private static List<String> classlist=new ArrayList<>();
    private static List<String > awardsList=new ArrayList<>();
    private static String schoolNameInput="";

    /**
     * 迷幻的初始化
     * @param excel_path 传进来的excel的路径
     * @param out_path  输出的文件夹路径
     */
    public MainActivity(String excel_path, String out_path){
        WT_PATH="wordTemplate/wt4.doc".replace("\\\\","/");
        EXCEL_PATH=excel_path.replace("\\\\","/");
        OUTPUT_PATH=out_path.replace("\\\\","/");
        list_ofData=FileUtil.readExcel(EXCEL_PATH);
        marksMap=getMarksMap(list_ofData);
    }

    /**
     * 迷幻的初始化之2
     * @param excel_path    同上
     * @param out_path      同上
     * @param wt_path   模板的路径，方便用户自定义模板
     */
    public MainActivity(String excel_path, String out_path,String wt_path,String schoolName){
        WT_PATH=wt_path.replace("\\\\","/");
        EXCEL_PATH=excel_path.replace("\\\\","/");
        OUTPUT_PATH=out_path.replace("\\\\","/");
        list_ofData=FileUtil.readExcel(EXCEL_PATH);
        marksMap=getMarksMap(list_ofData);
        schoolNameInput=schoolName;
    }

    public void mainAct() throws Exception{

        //先对奖项进行分组
//        Set<Map<String,Object>> awardsSet=new HashSet<>();
        Set<String > awardsSet=new HashSet<>();

        Set<String> classSet=new HashSet<>();
        //一个固定的awards序列
        for(Map map:list_ofData){
            String awards=(String )map.get("award");
            awardsSet.add(awards);
        }
        for(String str:awardsSet){
            awardsList.add(str);
        }
        //一个固定的class序列
        for(Map map:list_ofData){
            String class_=(String )map.get("class");
            classSet.add(class_);
        }
        for (String str:classSet){
            classlist.add(str);
        }
        classlist.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        //此数组记录各个奖项具体到每个班级的数量，arr[奖项编号][班级数量]
        int[][] arrOfAwardsNumInAClass=new int[awardsSet.size()][classSet.size()];
        for (int i=0;i<arrOfAwardsNumInAClass.length;i++){
            for(int j=0;j<arrOfAwardsNumInAClass[0].length;j++){
                arrOfAwardsNumInAClass[i][j]=0;
            }
        }
        //小天使说同一个奖项同一个班级最高获奖人数就限定在20以内吧
        String[][][] arrOfAll=new String[awardsSet.size()][classSet.size()][20];
        //String[奖项编号][班级编号][班级里面同一个奖项获奖编号]=学生名字；
        for(Map map:list_ofData){
            String className=(String )map.get("class");
            String stuName=(String )map.get("name");
            String award=(String )map.get("award");
            int awardsNum=awardsNum(awardsList,award);
            int classsNum=classsNum(classlist,className);
            arrOfAll[awardsNum][classsNum][arrOfAwardsNumInAClass[awardsNum][classsNum]++]=stuName;
        }

        //搞个数组，存储每个奖项总共有多少个班级，凭借这个数据进行word的分页
        int arrOfClassNumInAwards[]=new int[awardsList.size()];
        for(int i=0;i<arrOfClassNumInAwards.length;i++){
            arrOfClassNumInAwards[i]=0;
        }
        for(int i=0;i<awardsList.size();i++){
            for (int j=0;j<classlist.size();j++){
                if(arrOfAwardsNumInAClass[i][j]!=0){
                    arrOfClassNumInAwards[i]+=1;
                }
            }
        }
        for(int i=0;i<awardsList.size();i++){
            for(int j=0;j<classlist.size();j++){
                for(int k=0;k<arrOfAwardsNumInAClass[i][j];k++){
                        System.out.println(awardsList.get(i)+","
                                +classlist.get(j)+","+arrOfAll[i][j][k]+","+
                                marksOfAwards(awardsList.get(i)));
                }
            }
        }
        wordOutPut(arrOfAll,arrOfAwardsNumInAClass,arrOfClassNumInAwards);
//        readAll();

    }

    private static void readAll(){
        List<Map<String ,Object>> list_ofData=FileUtil.readExcel(EXCEL_PATH);
        for(Map map:list_ofData){
            String schoolName=(String )map.get("school");
            String className=(String )map.get("class");
            String stuName=(String )map.get("name");
            String award=(String )map.get("award");
            String marks=(String )map.get("marks");
            System.out.println(schoolName+","+className+","+stuName+","+award+","+marks);
        }
    }

    /**
     * @return 返回奖项所在的一维数组编号
     */
    private static int awardsNum(List<String> awardsList,String awards){
//        int i=0;
        for(int i=0;i<awardsList.size();i++){
            String str=awardsList.get(i);
            if(str.equals(awards)){
                return i;
            }
        }
        return 10010;
    }
    /**
     * @return 返回班级所在的二维数组编号
     */
    private static int classsNum(List<String> classList,String class_){
        for(int i=0;i<classList.size();i++){
            if(classList.get(i).equals(class_)){
                return i;
            }
        }
        return 10010;
    }

    /**
     * 生成Word
     * 一开始下面这段代码只有我和上帝看得懂
     * 过了一阵子之后，这段代码就只有上帝自己看得懂了
     *
     * @param data  数据 data[奖项编号][班级编号][人员编号]
     * @param awardsNum awardsNum[奖项编号][班级数量]=这个班级获得这个奖项的人数
     * @param classInAwards classInAwards[奖项编号]=这个奖项总共有多少个班级获得
     * @throws Exception IO异常
     */
    public static void wordOutPut(String[][][] data,int[][] awardsNum,int[] classInAwards) throws Exception{
        for(int i=0;i<data.length;i++){     //________________________________i是奖项的编号

            if(classInAwards[i]%4==0){      //这个奖项最后一页套用wt4
                int ye=classInAwards[i]/4;
                int xunFlag=0;//循环的flag
                int ye_newID=0;//生成的文件名的索引
                for(int j=0;j<ye;j++) {          //每页每页的生成
                    InputStream is = new FileInputStream(WT_PATH);      //导入模板
                    HWPFDocument doc = new HWPFDocument(is);
                    Range range = doc.getRange();       //获取这一页模板上面的文字
                    range.replaceText("${schoolName}", schoolNameInput);
                    range.replaceText("${marks}", marksOfAwards(awardsList.get(i)));
                    range.replaceText("${year}",""+Calendar.getInstance().get(Calendar.YEAR));
                    range.replaceText("${award}", awardsList.get(i));
                    int class_flag=1;//作为 替换${stuname1},${stuname2}的索引
                    for(int k=1;k<k+4;k++){           //k保证了足够4组班级——学生的数据之后才换
                        if(xunFlag>=classlist.size()){
                            break;
                        }if(k>=5){
                            break;
                        }
                        if(awardsNum[i][xunFlag]!=0){
                            //这个奖项的这个班级里面有人,构造classname数据
                            String class_name_X = classlist.get(xunFlag);       //数据
                            String stu_name_X = "";
                            for(int m=0;m<awardsNum[i][xunFlag];m++){       //_____m学生编号，xunFlag班级编号，i是奖项编号
                                //构造stuname数据
                                stu_name_X = stu_name_X + data[i][xunFlag][m] + "    ";
                            }
                            range.replaceText("${clssName" + class_flag + "}", class_name_X);
                            range.replaceText("${stuName" + (class_flag++) + "}", stu_name_X);
//                            k++;
                            xunFlag++;
                        }else {
                            //这个奖项的这个班级里面没有人
                            xunFlag++;
                            k--;
                        }
                    }

                    OutputStream os = new FileOutputStream(OUTPUT_PATH + awardsList.get(i) + (ye_newID++) + ".doc");
                    //把doc输出到输出流中
                    doc.write(os);
                    //关闭输出流，这里每一页的生成，都对应一个输入流和一个输出流，所以每一页生成完毕之后都要关闭一次输入输出流
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //关闭输入流
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }else {          //这个奖项最后一页套用wt1或者2或者3
                int ye=classInAwards[i]/4;
                int yeLast=classInAwards[i]%4;
                int xunFlag=0;//班级循环访问的flag，每个奖项遍历开始时候重置一遍
                int ye_newID=0;//生成的文件名的索引
                for(int j=0;j<ye;j++) {          //每页每页的生成
                    InputStream is = new FileInputStream(WT_PATH);      //导入模板
                    HWPFDocument doc = new HWPFDocument(is);
                    Range range = doc.getRange();       //获取这一页模板上面的文字

                    //这个页面上面一些共有信息的初始化
                    range.replaceText("${schoolName}", schoolNameInput);
                    range.replaceText("${marks}", marksOfAwards(awardsList.get(i)));
                    range.replaceText("${year}",""+Calendar.getInstance().get(Calendar.YEAR));
                    range.replaceText("${award}", awardsList.get(i));

                    //开始为这个页面填充具体的className和stuName
                    int class_flag=1;//作为 替换${stuname1},${stuname2}的索引
                    int k=1;
                    while (k<5){           //k保证了足够4组班级——学生的数据之后才换
                        if(xunFlag>=classlist.size()){
                            break;
                        }
                        if(awardsNum[i][xunFlag]!=0){
                            //这个奖项的这个班级里面有人,构造classname数据
                            String class_name_X = classlist.get(xunFlag);       //数据
                            String stu_name_X = "";
                            for(int m=0;m<awardsNum[i][xunFlag];m++){       //_____m学生编号，xunFlag班级编号，i是奖项编号
                                //构造stuname数据
                                stu_name_X = stu_name_X + data[i][xunFlag][m] + "    ";
                            }
                            range.replaceText("${clssName" + (class_flag%4+1) + "}", class_name_X);
                            range.replaceText("${stuName" + (class_flag%4+1) + "}", stu_name_X);
                            class_flag++;
                            k++;
                            xunFlag++;
                        }else {
                            //这个奖项的这个班级里面没有人
                            xunFlag++;
                        }
                    }

                    OutputStream os = new FileOutputStream(OUTPUT_PATH + awardsList.get(i) + (ye_newID++) + ".doc");
                    //把doc输出到输出流中
                    doc.write(os);
                    //关闭输出流，这里每一页的生成，都对应一个输入流和一个输出流，所以每一页生成完毕之后都要关闭一次输入输出流
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //关闭输入流
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                InputStream is = new FileInputStream(WT_PATH.replace("wt4","wt"+yeLast));      //导入模板
                HWPFDocument doc = new HWPFDocument(is);
                Range range = doc.getRange();       //获取这一页模板上面的文字
                range.replaceText("${schoolName}", schoolNameInput);
                range.replaceText("${marks}", marksOfAwards(awardsList.get(i)));
                range.replaceText("${year}",""+Calendar.getInstance().get(Calendar.YEAR));
                range.replaceText("${award}", awardsList.get(i));
                int class_flag=1;//作为 替换${stuname1},${stuname2}的索引
                for(int k=1;k<k+4;){           //k保证了足够4组班级——学生的数据之后才换
                    if(xunFlag>=classlist.size()){
                        break;
                    }
                    if(k>=5){
                        break;
                    }
                    if(awardsNum[i][xunFlag]!=0){
                        //这个奖项的这个班级里面有人,构造classname数据
                        String class_name_X = classlist.get(xunFlag);       //数据
                        String stu_name_X = "";
                        for(int m=0;m<awardsNum[i][xunFlag];m++){       //_____m学生编号，xunFlag班级编号，i是奖项编号
                            //构造stuname数据
                            stu_name_X = stu_name_X + data[i][xunFlag][m] + "    ";
                        }
                        range.replaceText("${clssName" + class_flag + "}", class_name_X);
                        range.replaceText("${stuName" + class_flag+ "}", stu_name_X);
                        class_flag++;
                        k++;
                        xunFlag++;
                    }else {
                        //这个奖项的这个班级里面没有人
                        xunFlag++;
                    }
                }


                OutputStream os = new FileOutputStream(OUTPUT_PATH + awardsList.get(i) + (ye_newID++) + ".doc");
                //把doc输出到输出流中
                doc.write(os);
                //关闭输出流，这里每一页的生成，都对应一个输入流和一个输出流，所以每一页生成完毕之后都要关闭一次输入输出流
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //关闭输入流
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 通过奖项-分值的表，查询奖项对应的分值
     * @param awardsName    奖项的名字
     * @return  返回奖项对应的分值
     */
    private static String marksOfAwards(String awardsName){
        return (String)marksMap.get(awardsName);
    }

    /**
     * 制作一个奖项-分值的Map
     * @param list  数据来源
     * @return  返回的分值表
     */
    private static HashMap<String,Object> getMarksMap(List<Map<String ,Object>> list){
        HashMap<String,Object> map=new HashMap<>();
        for(Map mapOfList:list){
            map.put((String )mapOfList.get("award"),mapOfList.get("marks"));
        }
        return map;
    }

    protected void finalize() throws java.lang.Throwable {
        super.finalize();
        System.out.println("这个对象被回收");
    }

}