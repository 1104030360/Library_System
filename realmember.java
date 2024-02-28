import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JOptionPane;
import java.time.LocalDate;
public   class realmember 
{
public String input5;
public String input1;
public String input2;
public String input6;
public String  input8;
public String  input9;


////編輯書籍要寫(在admin)
    public void function()
    {
        String option[]={"借書","還書","查書"};
        String option3[]={"ID","書名"};
        String option4="";
        String option5="";
        LocalDate d = LocalDate.now(); // 取得今日
    	LocalDate d2= LocalDate.of(2020, 1, 17); 
        for(int counter10=0;counter10<Book.oldbook.size();counter10++)
        {
        option4+=Book.oldbook.get(counter10)+"\n";
        }
        for(int counter11=0;counter11<Book.bookID.size();counter11++)
        {
        option5+=Book.bookID.get(counter11)+"\n";
        }
        JOptionPane.showMessageDialog(null,Book.getbookname2(),"輸出",0);
        if(UserClass.operation3!=1)
        {
            int input=JOptionPane.showOptionDialog(null, "選擇一項功能","輸入", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,null, option,option[0]);
            int input7=JOptionPane.showOptionDialog(null, "使用輸入書的ID或是書名","輸入", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,null, option3,option3[0]); 
    
        if(input7==0)
        {  
            input8=JOptionPane.showInputDialog(null,"選擇書的ID\n"+option5,"輸出",0);
            //input5=JOptionPane.showInputDialog(null,"請輸入書的ID","輸入",0);
        }
        else if(input7==1)
        {
            input9=JOptionPane.showInputDialog(null,"選擇書的名字\n"+option4,"輸入",0);
            //input1=JOptionPane.showInputDialog(null,"請輸入書的名字","輸入",0);
        }
        input2=JOptionPane.showInputDialog(null,"請輸入書的作者","輸入",0);//先做一個從書名找從作者找之後再做
        input6=JOptionPane.showInputDialog(null,"請輸入書的出版社","輸入",0);
        
        
        Book.setbookname1(input8,input,input9,input2,input6,input7);
        JOptionPane.showMessageDialog(null,Book.getbookname1(),"輸出",0);
        if(input==0)
        {
            if(Book.getbookname1()!=null)
            {
            JOptionPane.showMessageDialog(null,"借書時間是:"+d.toString()+"\n"+"還書日期是:"+d.plusWeeks(2L),"輸出",0);
            }
        }
        
    }
}
}
        

            /*if(input==1)
            {
            int question=JOptionPane.showOptionDialog(null,"是否過期","輸入", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,null,option2,option2[0]);
            String question1=JOptionPane.showInputDialog(null,"借書到還書過了幾天","輸入",0);
            book func1=new book(question,question1);
            //book func3=new book(question1);
            }
            int z=func3.getoverdue();

            if(func1.getreturnbook()!=null)//先還完書再罰款
            {
                JOptionPane.showMessageDialog(null,"恭喜還書成功你還的書是"+func1.getreturnbook(),"輸出",0);
            }
            if(question==0)
            {

                number2=1;
            }//直接在studentclass定義一個public variable=固定數字然後在bookclass也定義一個基本該交的變數，之後realmemberclass寫入自己還書的起訖日兩著相減後傳入bookclass然後再傳入bookclass再把傳入值根基本要繳交的天數相減然後從studentclass get 那個相減得值，在比大小大於等於student規定的天數就罰款(一天10塊兩天100塊三天1000塊是最多了)
            else{number2=0;}//還沒打完

            break;
            case 2://搜尋書
            ArrayList<String>input8=new ArrayList<String>();
            String input6=JOptionPane.showInputDialog(null, "書作者是誰","輸入",0);//之後再補
            int input7=JOptionPane.showOptionDialog(null,"想找幾本書(最多三本書最少一本書)","輸出", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,null,option1, option1[0]);
            for(int counter=0;counter<input7+1;counter++)
            {
                String input5=JOptionPane.showInputDialog(null,"請問要搜尋哪本書","輸入",0);
                input8.add(input5);
            }
            book func2=new book(input7,input8);            
            JOptionPane.showMessageDialog(null,"你要找的書是"+func2.getbookname1(),"輸出",0);
            break;*/
    


