public class Staff_1 extends Member_1
{
    public static Book [] Borrowed_personal =new Book[50];
    String personaldata="";
    public static int flag=-1;
	
	public Staff_1(String name,int password,String identity,Book [] personalBook)
	{
		super(name,password,identity);
		
		Borrowed_personal=personalBook;
	}
	
	public static void storeStaff(Staff_1 staff)
	{
		for(int i=0;i<=UserClass.store_staff.length;i++)
		{
			if(UserClass.store_staff[i]==null)
			{
				UserClass.store_staff[i]=staff;
				break;
			}
		}
	}
	
	public String checkBorrowedBook()
	{
		return ""; 
	}
	
	public String checkPersonalInfo() 
	{
		
		return null;
	}
	
	public String toString()
	{
		String passwordStr = String.valueOf(getPassword());
		String length_password = "";
		for(int i=0;i<passwordStr.length();i++)
		{length_password+="*";}
		return personaldata+="使用者名稱:"+getName()+"\n密碼:"+length_password+"\n身分別:"+getIdentity();
	}

	public static boolean checkName(String Name)
	{
		for(int i=0;i<UserClass.store_staff.length;i++)
		{
			if(UserClass.store_staff[i] != null &&
			   Name.equals(UserClass.store_staff[i].getName()))
			{
				flag=i;
				return true;
			}
		}
		return false;
	}

	public static boolean checkPassword(int password) 
	{
		if(password==UserClass.store_staff[flag].getPassword())
		{return true;}
		else
		{return false;}
	}
}
