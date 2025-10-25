import javax.swing.*;
import java.util.ArrayList;
public abstract class Admin 
{
	private AdminUser nowAccount = new AdminUser(null,0,null);
	protected static ArrayList<AdminUser> allAccounts = new ArrayList<AdminUser>(); 
	
	public Admin()
	{
		allAccounts.add(new AdminUser("0001",1111,"館長"));
		allAccounts.add(new AdminUser("0002",2222,"員工"));
		allAccounts.add(new AdminUser("0003",3333,"員工"));
		allAccounts.add(new AdminUser("0004",4444,"員工"));
		allAccounts.add(new AdminUser("0005",5555,"員工"));
		allAccounts.add(new AdminUser("0006",6666,"員工"));
	}
	
	public void setNowAccount(AdminUser m)
	{nowAccount=m;}
	public AdminUser getNowAccount()
	{return nowAccount;}
	
	public abstract boolean checkPassword(int password);  //抽象方法，會交由Boss與Employee進行override
	
	public abstract boolean checkAccount(String account);    //抽象方法，會交由Boss與Employee進行override
	
	public static void adminuser()
	{
		String option[]={"新增","刪除","查詢","編輯"};
	    String option1[]={"ID","書名","作者","出版社"};
	    int input1;
	    input1=JOptionPane.showOptionDialog(null,"要選擇新增、刪除或查詢書籍呢?","輸入",JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE,null,option,option[0]);
	    if(input1==3)
	    {
	        JOptionPane.showMessageDialog(null,"目前館藏有:"+"\n"+Book.getbookname(),"輸出",0);
	        String input5=JOptionPane.showInputDialog(null,"請輸入要修改書籍的ID","輸入",0);
	        int editoption=JOptionPane.showOptionDialog(null,"請問要修改什麼","輸入",JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE,null,option1,option1[0]);
	        String editvalue=JOptionPane.showInputDialog(null,"請輸入修改值","輸入",0);
	        Book.editbook(input5,editoption,editvalue);
	        JOptionPane.showMessageDialog(null,Book.geteditbook(),"輸出",0);
	    }
	    else
	    {
	        String input4=JOptionPane.showInputDialog(null,"請輸入書的ID","輸入",0);
	        String input=JOptionPane.showInputDialog(null,"請輸入書的書名","輸入",0);
	        String input2=JOptionPane.showInputDialog(null,"請輸入書的作者姓名","輸入",0);
	        String input3=JOptionPane.showInputDialog(null,"請輸入書的出版社名稱","輸入",0);

	        Book.setbookname(input1,input4,input,input2,input3);
	        if (input1==0) 
	        {JOptionPane.showMessageDialog(null,"目前館藏有:"+"\n"+Book.getbookname(),"輸出",0);}
	        else if(input1==1)
	        {JOptionPane.showMessageDialog(null,"目前館藏有:"+"\n"+Book.getbookname(),"輸出",0);}
	        else if(input1==2)
	        {JOptionPane.showMessageDialog(null,"你所查詢的書的狀態是"+"\n"+Book.getbookname(),"輸出",0);}
	        else if(input1==3)
	        {JOptionPane.showMessageDialog(null,"你所查詢的書的狀態是"+"\n"+Book.getbookname(),"輸出",0);}
	    }
	}
}