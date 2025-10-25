import javax.swing.JOptionPane;

public class Employee extends Admin
{
	public static int signal_rest=0;
	public static int salary=30000;
	public static int bonus=0;
	int operation2,operation3;
	
	private static String option2[]={"回主頁","繼續使用功能","登出並關閉"};
	String option3_2[]={"查看月薪","查看今日班表","更改書籍資訊"};
	
	public void checkSalary()  //利用JOptionPane來接續程式，員工可以查看自身的薪水
	{
		int total=salary+bonus;
		JOptionPane.showMessageDialog(null,"您的本月薪資為"+total);
		Jump();
	}
	
	public void checkWork()    //利用JOptionPane來接續程式，讓員工可以檢查自身班表
	{
		if(signal_rest==1)
		{
			JOptionPane.showMessageDialog(null,"今天不上班!,假期愉快~");
			Jump();
		}
		
		if(signal_rest==0)
		{
			JOptionPane.showMessageDialog(null,"今天的工作為維護與校對書籍!");
			Jump();
		}
	}
	
	public boolean checkAccount(String account)    //檢查此帳號是不是預設的Employee帳號(多個)，回傳true&false
	{
		int x=0;
		for(AdminUser a:allAccounts)  //?
		{
			if(account.equals(a.getName()) && a.getIdentity().equals("員工"))
			{
				x++;
				setNowAccount(a);
				break;
			}
		}
		if(x==1)
			{return true;}
		else
			{return false;}
	}
	
	public boolean checkPassword(int password)  //檢查此密碼是不是對應的Employee密碼，回傳true&false
	{
		if(getNowAccount().getPassword()==password)
		{return true;}
		else
		{return false;}
	}
	
	public void Jump()
	{
		operation2=JOptionPane.showOptionDialog(null,"還要繼續使用系統嗎",null,1,1,null,option2,null);
		if(operation2==0)
		{UserClass.EnterUser();}
		if(operation2==1)
		{
			operation3=JOptionPane.showOptionDialog(null,"請問還要使用甚麼功能呢?","館員系統",1,1,null,option3_2,null);
			//館員進入功能選項
			if(operation3==0)     //使用功能:查看月薪					    
				{checkSalary();}
			if(operation3==1)     //使用功能:查看今日班表
				{checkWork();}
			if(operation3==2)     //使用功能:更改書籍
				{Admin.adminuser();}
		}
		if(operation2==2)
		{System.exit(0);}
	}
}
