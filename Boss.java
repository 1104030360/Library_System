import javax.swing.*;

public class Boss extends Admin
{
	private static String option1[]={"今日為工作日","今日休假"};
	private static String option2[]={"回主頁","繼續使用功能","登出並關閉"};
	
	public boolean checkAccount(String account)   //檢查此帳號是不是預設的Boss帳號，回傳true&false
	{
		int x=0;
		for(AdminUser a:allAccounts)
		{
			if(account.equals(a.getName()) && a.getIdentity().equals("館長"))
			{
				x++;
				setNowAccount(a);
				//System.out.print(getNowAccount().getPassword());  偵錯用
 				break;
			}
		}
		if(x==1)
		{return true;}
		else
		{return false;}
	}
	
	public boolean checkPassword(int password)
	{
		if(getNowAccount().getPassword()==password)
		{return true;}
		else
		{return false;}		
	}
	
	public void Rest()        //使用JOptionPane來接續功能，控制員工是否休假(?)
	{
		Employee.signal_rest=JOptionPane.showOptionDialog(null,"今天要發出放假公告嗎?","上班通知",1,1,null,option1,null);
		if(Employee.signal_rest==0)
		{
			JOptionPane.showMessageDialog(null,"以調整今日為工作日!");
			Jump();
		}
		
		if(Employee.signal_rest==1)
		{
			JOptionPane.showMessageDialog(null,"已發出休假公告,祝您有美好的一天");
			Jump();
		}
	}
	
	public void Salary()      //使用JOptionPane來接續功能，控制員工薪水
	{
		Employee.bonus=Integer.parseInt(JOptionPane.showInputDialog(null,"請輸入員工的加薪幅度(起薪30000)","加薪系統"));
		int newSalary=Employee.bonus+Employee.salary;
		JOptionPane.showMessageDialog(null, "已更新員工薪水至"+newSalary+"!");
		Jump();
	}
	
	public void Jump()   //增加寫程式的結構性
	{
		int operation,operation3;
		String option3_1[]={"決定是否休館","控制員工月薪","編輯書籍"};
		
		operation=JOptionPane.showOptionDialog(null,"還要繼續使用您的功能嗎","登出選項",1,1,null,option2,null);
		if(operation==0)
			{UserClass.EnterUser();}
		if(operation==1)
			{
				operation3=JOptionPane.showOptionDialog(null,"館長您好，請點擊你要使用的功能","館長系統",1,1,null,option3_1,null);
				//館長進入功能選項
				if(operation3==0)     						    
					{Rest();}       //使用功能:是否休館
				if(operation3==1)     
					{Salary();}     //使用功能:控制月薪
				if(operation3==2)     
					{Admin.adminuser();} //使用功能:編輯書籍
			}
		if(operation==2)
			{System.exit(0);}
	}
}