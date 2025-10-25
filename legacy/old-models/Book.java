import java.util.ArrayList;
import java.util.Arrays;
import java.util.*;
public  class Book
{
    public static int returnbookoption3;
    public static int returnbookoption4;
    public static int returnbookoption5;
    public static int returnborrowoption;
    public static int returnborrowoption1;
    public static int returnborrowoption6;
    public static int returnborrowoption7;

    public static String returnbookid3;
    public static String returnbookid4;
    public static String returnbookid5;
    public static String returnbookid6;
    public static String returnbookname7;

    public static String returnbookname3;
    public static String returnbookname4;
    public static String returnbookname5;
    public static String returnbookwritter5;
    public static String returnbookpublisher5;
    public static int returnbookoption6;
    public static int returnbookoption7;


public static String returnbookwritter;
public static int returnbookoption=3;
public static String returnbookname;
public static String returnbookId;
public static String returnbookpublsher;
public static int returnbookoption1;
public static int returnbookoption2;
public static String  returnbookname10;
public static String returnbookId1;
public static String returnbookpublisher1;
public static String returnbookwritter1;
public static String onthedesk;
public static String returneditID;
public static String returneditvalue;
public static int returneditoption;
public static int index3;
public static int check5;
public static int check10;
public static int check11;
public static int check12;
public static int check13;
public static int check14;
public static int check15;
public static int check16;
public static int check17;
public static int returnoverdue;
public static int returnbooknumber;
public static int returnoverdate;
public static int date1=7;
public static int counter6=0;
public static ArrayList<String> oldbook=new ArrayList<String>();
public static ArrayList<String> writters=new ArrayList<String>();
public static ArrayList<String> publisher=new ArrayList<String>();
public static ArrayList<String> bookID=new ArrayList<String>();

public  static void initialize()
{ 
    
    oldbook.add("Java");
    oldbook.add("管數課本");
    oldbook.add("英文課本");
    oldbook.add("國文課本");
    oldbook.add("體育課本");

    writters.add("吳柏毅");
    writters.add("吳昀蓁");
    writters.add("林俊廷");
    writters.add("屠安弟");
    writters.add("陳重言大帥哥");

    publisher.add("中央大學");
    publisher.add("台灣大學");
    publisher.add("交通大學");
    publisher.add("政治大學");
    publisher.add("清華大學");

    bookID.add("001");
    bookID.add("002");   
    bookID.add("003");
    bookID.add("004");
    bookID.add("005");
}
public static void setbookname1(String  bookID1 ,int bookoption1,String bookname1,String bookwritter1,String bookpublisher1,int bookoption2)//會員借書還書的地方(全)
{

    if(bookoption1==0)
    {
        if(bookoption2==1)//使用name查
        {
        boolean r=oldbook.contains(bookname1);
        if(r)
        {
            int index10=oldbook.indexOf(bookname1);
            bookID.remove(index10);
            oldbook.remove(index10);
            writters.remove(index10);
            publisher.remove(index10);
            check5=1;
        }
        else{check5=0;}
        }
        else if(bookoption2==0)
        {
            boolean v=bookID.contains(bookID1);
            if(v)
            {
                int index11=bookID.indexOf(bookID1);
                bookID.remove(index11);
                oldbook.remove(index11);
                writters.remove(index11);
                publisher.remove(index11);
                check5=1;

            }
            else{check5=0;}

        }
    }
    else if(bookoption1==1)
    {   
        oldbook.add(bookname1);
        writters.add(bookwritter1);
        bookID.add(bookID1);
        publisher.add(bookpublisher1);
    }
    else if(bookoption1==2)
    {
        if(bookoption2==1)//使用id查
        {
            boolean r=oldbook.contains(bookname1);
            if(r)
            {
                int index12=oldbook.indexOf(bookname1);

            }
        }
        else if(bookoption2==0)
        {
            boolean v=bookID.contains(bookID1);
            if(v)
            {
                int index13=bookID.indexOf(bookID1);

            }

        }
    }
    returnbookname10=bookname1;
    returnbookoption1=bookoption1;
    returnbookoption2=bookoption2;
    returnbookwritter1=bookwritter1;
    returnbookId1=bookID1;
    returnbookpublisher1=bookpublisher1;
    System.out.println(returnbookname10);

}
public static void setbookname3(int borrowoption,int bookoption3,String bookid3)//id borrow
{
    if(bookoption3==0)
    {
        boolean v=bookID.contains(bookid3);
        if(v)
        {
            int index11=bookID.indexOf(bookid3);
            bookID.remove(index11);
            oldbook.remove(index11);
            writters.remove(index11);
            publisher.remove(index11);
            check5=1;

        }
        else{check5=0;}

    }
    returnbookoption3=bookoption3;
    returnbookid3=bookid3;
    returnborrowoption=borrowoption;


}

public static void setbookname4(int borrowoption,int bookoption4,String bookname4)//name borrow
{
    if(bookoption4==1)//使用name查
    {
    boolean r=oldbook.contains(bookname4);
    if(r)
    {
        int index10=oldbook.indexOf(bookname4);
        bookID.remove(index10);
        oldbook.remove(index10);
        writters.remove(index10);
        publisher.remove(index10);
        check5=1;
    }
    else{check5=0;}
    }
    returnbookoption4=bookoption4;
    returnbookname4=bookname4;
    returnborrowoption=borrowoption;
    System.out.println(returnborrowoption);

}
public static void setbookname5(int borrowoption,String bookid5,String bookname5,String bookwritter5,String bookpublisher5)//還書
{
    if(borrowoption==1)
    {   
        oldbook.add(bookname5);
        writters.add(bookwritter5);
        bookID.add(bookid5);
        publisher.add(bookpublisher5);
    }
    returnborrowoption=borrowoption;
    returnbookid5=bookid5;
    returnbookwritter5=bookwritter5;
    returnbookpublisher5=bookpublisher5;
    returnbookname5=bookname5;
    if(oldbook.contains(bookname5)||bookID.contains(bookid5)){
    System.out.println(bookname5+bookid5);
    }
}
public static void setbookname6(int borrowoption,int bookoption6,String bookid6)
{
    returnborrowoption=borrowoption;//seacrh
    returnbookoption6=bookoption6;//id
    returnbookid6=bookid6;
    System.out.println();
}
public static void setbookname7(int borrowoption1,int bookoption7,String bookname7)
{
    returnborrowoption1=borrowoption1;//search
    returnbookoption7=bookoption7;//name
    returnbookname7=bookname7;
    System.out.println(returnbookname7);

} 
public static void setbookname( int bookoption,String  bookId ,String bookname,String bookwritter,String bookpublisher)//管理員新增刪除書籍的地方(全)
{
    
    if(bookoption==0)
    {
    oldbook.add(bookname);
    }
    if(bookoption==0)
    {
        writters.add(bookwritter);
    }
    if(bookoption==0)
    {
        publisher.add(bookpublisher);
    }
    if(bookoption==0)
    {
        bookID.add(bookId);
    }
    if(bookoption==1)
    {
        if(oldbook.contains(bookname))
        {
        oldbook.remove(bookname);
        check10=1;
        }
        else{check10=0;}
    }

    if(bookoption==1)
    {
        if(writters.contains(bookwritter))
        {
        writters.remove(bookwritter);
        check11=1;
        }
        else{check11=0;}
    }
 
    if(bookoption==1)
    {
        if(publisher.contains(bookpublisher))
        {
        publisher.remove(bookpublisher);
        check12=1;
        }
        else{check12=0;}
    }
else{check12=0;}

    if(bookoption==1)
    {
        if(bookID.contains(bookId))
        {
        bookID.remove(bookId);
        check13=1;
        }
        else{check13=0;}
    }

    if(oldbook.contains(bookname))
    {
        check14=1;
    }
    else{check14=0;}
    if(writters.contains(bookwritter))
    {
        check15=1;
    }
    else{check15=0;}
    if(publisher.contains(bookpublisher))
    {
        check16=1;
    }
    else{check16=0;}
    if(bookID.contains(bookId))
    {
        check17=1;
    }
    else{check17=0;}

    returnbookname=bookname;
    returnbookoption=bookoption;
    returnbookwritter=bookwritter;
    returnbookId=bookId;
    returnbookpublsher=bookpublisher;

}
public static void editbook(String editID,int editoption,String editvalue)//editoption:要改什麼 editID要找的ID
{
    returneditID=editID;
    returneditoption=editoption;
    returneditvalue=editvalue;
    System.out.println(returneditID+returneditoption+returneditvalue);


}
public static String geteditbook()
{
    if(bookID.contains(returneditID))
    {
        int index5=bookID.indexOf(returneditID);//找到書
        if(returneditoption==0)//id
        {
            String vacancy=bookID.get(index5);
            bookID.remove(vacancy);
            bookID.add(index5,returneditvalue);
            return "更改後的ID為"+bookID.get(index5);
        }
        else if(returneditoption==1)//書名
        {
            String vacancy1=oldbook.get(index5);
            oldbook.remove(vacancy1);
            oldbook.add(index5,returneditvalue);  
            return "更改後的書名為"+oldbook.get(index5);
        }
        else if(returneditoption==2)//作者
        {
            String vacancy2=writters.get(index5);
            writters.remove(vacancy2);
            writters.add(index5,returneditvalue); 
            return "更改後的作者為"+writters.get(index5); 
        }
        else if(returneditoption==3)//出版社
        {
            String vacancy3=publisher.get(index5);
            publisher.remove(vacancy3);
            publisher.add(index5,returneditvalue);  
            return "更改後的出版社為"+publisher.get(index5);
        }
        else
        {
            return "無此更改選項";
        }
    }
    else
    {
        return null;
    }
}
public static String getbookname()//admin use
{
    String output2="";
    for(int counter4=0;counter4<oldbook.size();counter4++)
    {
    output2+="書名:"+oldbook.get(counter4)+"ID:"+bookID.get(counter4)+oldbook.get(counter4)+" "+"作者:"+writters.get(counter4)+" "+"出版社:"+publisher.get(counter4)+"\n";
    }
    if(returnbookoption==0)
    {
    int index0;
    index0=oldbook.indexOf(returnbookname);
    return output2+"新增 "+oldbook.get(index0);//新增書的return
    }
    else if(returnbookoption==1)
    {
        if(check10==1||check11==1||check12==1||check13==1)
        {
        return output2+"刪除"+returnbookname;
        }
        else{return output2+"你還的書:"+null;}//刪除書的return
    }
    else if(returnbookoption==2)
    {
        if(oldbook.contains(returnbookname)||bookID.contains(returnbookId)||writters.contains(returnbookwritter)||publisher.contains(returnbookpublsher))
        {
            returnbookoption=-1;
            if(check14==1)
            {
            return output2+"\n"+"你查詢的書是"+returnbookname+"\n";
            }
            else if(check15==1)
            {        
                return output2+"\n"+"你查詢的書作者是"+returnbookwritter+"\n";
            }
            else if(check16==1)
            {       
                 return output2+"\n"+"你查詢的書出版社是"+returnbookpublsher+"\n";
            }
            else if(check17==1)
            {        
                return output2+"\n"+"你查詢的書ID是"+returnbookId+"\n";
            }
            else{return null;}
        }
        else{return null;}
    }//查詢書的return
    else if(returnbookoption==3){return output2;}
    else{return "查無此書";}
}

public static String getbookname1()//member use
{


    String output3="";

    for(int counter4=0;counter4<oldbook.size();counter4++)
    {
    output3+="ID:"+bookID.get(counter4)+oldbook.get(counter4)+" "+"作者:"+writters.get(counter4)+" "+"出版社:"+publisher.get(counter4)+"\n";
    }
    if(returnbookoption1==0)
    {
        if(returnbookname10==null)

        {Boolean check0=bookID.contains(returnbookId1);
            int index4=bookID.indexOf(returnbookId1);
            if(check0!=true&&check5!=0){
             return "借書成功你借的書是 "+returnbookId1; }
             else
             {
                 return null;
             }
        }
        else if(returnbookId1==null)
        {Boolean check1=oldbook.contains(returnbookname10);
            int index33=oldbook.indexOf(returnbookname10);

            if(check1!=true&&check5!=0)
            {

                return "借書成功你借的書是 "+ returnbookname10; 
            }
            else
                {
                    return null;
                }       
        }
        else
        {
            return null;
        }
    }
    else if(returnbookoption1==1)
    {
        boolean a=bookID.contains(returnbookId1);
        boolean b=oldbook.contains(returnbookname10);
        if(a||b)
        {
        if(returnbookId1==null){ return "還書書成功你還的書是 "+returnbookname10; }
        else if(returnbookname10==null){return "還書成功你還的書是 "+returnbookId1;}
        else{return null;}
        }
        else{return null;}
    }
    else if(returnbookoption1==2)
    {
        if(oldbook.contains(returnbookname10)||bookID.contains(returnbookId1))
        {
        if(returnbookoption2==0){ return "你查的書是 "+returnbookId1; }
        else if(returnbookoption2==1){return "你查的書是"+returnbookname10;}
        else{return output3;}
        }
        else{return null;}
    }
    else
    {
        return null;
    }
}
public static String getbookname2()
{
    String output4="";
    for(int counter4=0;counter4<oldbook.size();counter4++)
    {
    output4+="ID:"+bookID.get(counter4)+oldbook.get(counter4)+" "+"作者:"+writters.get(counter4)+" "+"出版社:"+publisher.get(counter4)+"\n";
    }
    return output4;

}
public static String getbookname3()//userclass memeber
{
    {
        String output3="";
    
        for(int counter4=0;counter4<oldbook.size();counter4++)
        {
        output3+="ID:"+bookID.get(counter4)+oldbook.get(counter4)+" "+"作者:"+writters.get(counter4)+" "+"出版社:"+publisher.get(counter4)+"\n";
        }
        if(returnborrowoption==0)
        {
            if(returnbookname4==null)
    
            {Boolean check0=bookID.contains(returnbookid3);
                int index4=bookID.indexOf(returnbookid3);
                if(check0!=true&&check5!=0){
                 return "借書成功你借的書是 "+returnbookid3; }
                 else
                 {
                     return null;
                 }
            }
            else if(returnbookid3==null)
            {Boolean check1=oldbook.contains(returnbookname4);
                int index33=oldbook.indexOf(returnbookname4);
    
                if(check1!=true&&check5!=0)
                {
    
                    return "借書成功你借的書是 "+ returnbookname4; 
                }
                else
                    {
                        return null;
                    }       
            }
            else
            {
                return null;
            }
        }
        else if(returnborrowoption==1)
        {
            boolean a=bookID.contains(returnbookid5);
            boolean b=oldbook.contains(returnbookname5);
            if(a||b)
            { 
                return "還書書成功你還的書是 "+returnbookname5+returnbookid5; 
            }
            else{return "5";}////!!!
        }
    
        /*else if(returnbookoption5==0)
        {
            if(oldbook.contains(returnbookname5)||bookID.contains(returnbookId5))
            {
            if(returnbookoption5==0){ return "你查的書是 "+returnbookId1; }
            else if(returnbookoption2==1){return "你查的書是"+returnbookname10;}
            else{return output3;}
            }
            else{return null;}
        }*/
        else
        {
            return null;
        }
    }

}
public static String returnbook()
{
    String output3="";

    for(int counter4=0;counter4<oldbook.size();counter4++)
    {
    output3+="ID:"+bookID.get(counter4)+oldbook.get(counter4)+" "+"作者:"+writters.get(counter4)+" "+"出版社:"+publisher.get(counter4)+"\n";
    }
    if(returnborrowoption1==2||returnborrowoption==2)
    {
        if(oldbook.contains(returnbookname7)||bookID.contains(returnbookid6))
        {
        if(returnbookname7==null){ return "你查的書是 "+returnbookid6; }
        else if(returnbookid6==null){return "你查的書是"+returnbookname7;}
        else{return output3;}
        }
        else{return null;}
    }
    else
    {
        return null;
    }
}
}
/*public static String admingetbbokname()//回傳書名跟作者跟出版社跟狀態的地方
{
    String output0="";
        for(int counter3=0;counter3<oldbook.size();counter3++)
        {
        output0+=oldbook.get(counter3)+" "+writters.get(counter3)+"\n";
        }
       return output0;
}*/
/*public static String admingetbookwritter()//回傳作者的地方
{
    String output1="";
    for(int counter4=0;counter4<oldbook.size();counter4++)
    {
        output1+=writters.get(counter4)+"\n";
    }//作者陣列暫時不會用到
    return output1;
}*/

/*public void setremovebookname(String removebookname)//管理員刪除書籍的地方
{
    boolean d=oldbook.contains(removebookname);
    if(d)
    {
        for( counter6=0;counter6<oldbook.size();counter6++)
        {
            if(oldbook.get(counter6)==removebookname)
            {
                    oldbook.remove(oldbook.get(counter6));
                    break;
            }
        }
    }
    this.removebookname=removebookname;
}
public String getremovebookname()
{
    int index1;
    index1=oldbook.indexOf(this.removebookname);
    return oldbook.get(index1);
}*/
/*public static void setbookwritter(String bookwritter)//新增刪除書籍的作者的地方
{   
    if(returnbookoption==0)
    {
        writters.add(bookwritter);
    }
    if(bookoption==1)
    {
        writters.remove(bookwritter);
    }
    this.bookwritter=bookwritter;
}*/
/*public String getremovebookwritter()
{
    if(bookoption==0)
    {
    int index2;
    index2=writters.indexOf(this.bookwritter);
    return "新增"+writters.get(index2);
    }
    else if(bookoption==1)
    {
        return "刪除"+this.bookwritter;
    }
    else{return bookwritter;}

}*/
/*public static void setbookpublisher(String bookpublisher) 
{
    if(bookoption==0)
    {
        publisher.add(bookpublisher);
    }
    if(bookoption==1)
    {
        publisher.remove(bookpublisher);
    }
    returnbookpublisher=bookpublisher;
}
public static String getbookpublisher()
{
    return this.bookpublisher;
}*/
/*public void setID(String ID)
{
    if(bookoption==0)
    {
        bookID.add(ID);
    }
    if(bookoption==1)
    {
        bookID.remove(ID);
    }
    this.ID=ID;
}
public static String getID()
{
    return this.ID;
}*/
/*public void setremovebooknumber(double removebooknumber)
{
    this.removebooknumber=removebooknumber;
}
public double getremovebooknumber()
{
    return this.removebooknumber;
}*/

/*public static void setbookoption(int bookoption)//管理員選擇新增刪除的選項
{
    bookoption1=bookoption;
}
public static int getbookoption()
{
    return bookoption1;
}*/


/*public static void setbookoption1(int bookoption1)
{
    this.bookoption1=bookoption1;
}
public static int getbookoption1()
{
    return this.bookoption1;
}*/
/*public static void setSearchbookname(String Searchbookname)//給會員借還書的地方
{
    if(bookoption1==0)
    {
        boolean r=oldbook.contains(Searchbookname);
        if(r)
        {
            int index3=oldbook.indexOf(Searchbookname);
            bookID.remove(index3);
            oldbook.remove(index3);
            writters.remove(index3);
            publisher.remove(index3);
        }
    }
    else if(bookoption1==1)
    {   
        oldbook.add(Searchbookname);
        writters.add(bookwritter1);
        bookID.add(bookID1);
        publisher.add(bookpublisher1);
    }
    this.Searchbookname=Searchbookname;
}*/

/*public static String getSearchbbookname()
{   
    String output3="";
    for(int counter4=0;counter4<oldbook.size();counter4++)
    {
    output3+="ID:"+bookID.get(counter4)+oldbook.get(counter4)+" "+"作者:"+writters.get(counter4)+" "+"出版社:"+publisher.get(counter4)+"\n";
    }
    if(bookoption1==0)
    {
    return "借書成功你借的書是 "+this.Searchbookname; 
    }
    else if(bookoption1==1)
    {
    return "還書成功你還的書是 "+this.Searchbookname;
    }
    else if(bookoption1==2)
    {
        return "你查的書是"+this.Searchbookname;
    }
    else
    {
        return null;
    }
}*/
/*public static void setbookID1(String BookID1)
{
    this.bookID1=bookID1;
}
public static String getbookID1()
{
     return this.bookID1;
}*/
/*public static void setbookwritter1(String bookwritter1)
{
    this.bookwritter1=bookwritter1;
}
public static String getbookwritter1()
{
    return this.bookwritter1;
}*/
/*public static void setreturnbook(String returnbook)//還書的地方
{
    this.returnbook=returnbook;
    if(this.returnbook!=null)
    {
        oldbook.add(this.returnbook);
    }
}*/
/*public static void setbookpublisher1(String bookpublisher1)
{
    this.bookpublisher1=bookpublisher1;
}
public static String getbookpublisher1()
{
    return this.bookpublisher1;
}*/
/*public String getreturnbook()
{
    boolean b=oldbook.contains(this.returnbook);
    if(b)
    {
        return this.returnbook;
    }
    else
    {
        return null;
    }
}*/

/*public void setbookname1(ArrayList<String> bookname1)
{
    this.bookname1=bookname1;

}
public ArrayList<String> getbookname1()//找書的地方
{
    ArrayList<String>bookname2=new ArrayList<String>();
    for(int counter5=0;counter5<this.bookname1.size();counter5++)
    {
       boolean d=oldbook.contains(bookname1.get(counter5));
       if(d)
        {
           bookname2.add(bookname1.get(counter5));
        }
    }
    return bookname2;
}*/

/*public void setonthedesk(String onthedesk)//是否在架上這邊還沒做完
{
onthedesks.add(onthedesk);
}
public String getonthedesk()
{
    int y;
    int total2=0;
    boolean c;
    for(int counter2=0;counter2<onthedesks.size();counter2++)
    {
        c=Arrays.equals(writters.get(counter2),Searchbookonthedesk);//Searchbook是被管理員拿來找是否在架上
        total2+=1;
        if(c){break;}//b是布林值只有true跟false
    }
    return onthedesks.get(total2);
}*/
