public class Teacher_1 extends Member_1
{	
	
	public static Book [] Borrowed_personal = new Book [100];   //老師的個人借書庫
	String personaldata="";
	public static int flag=-1;
	
	public Teacher_1(String name,int password,String identity,Book [] personalBook)
	{
		super(name,password,identity);
		
		Borrowed_personal=personalBook;
	}
	
	public static void storeTeacher(Teacher_1 teacher)
	{
		for(int i=0;i<=UserClass.store_teacher.length;i++)
		{
			if(UserClass.store_teacher[i]==null)
			{
				UserClass.store_teacher[i]=teacher;
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
		String output="";
		for(int i=0;i<Borrowed_personal.length;i++)
		{
			if(Borrowed_personal[i]!=null)
			{
				//未完output+=Borrowed_personal[i]
			}
		}
		return output;
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
		for(int i=0;i<UserClass.store_teacher.length;i++)
		{
			if(UserClass.store_teacher[i] != null &&
			   Name.equals(UserClass.store_teacher[i].getName()))
			{
				flag=i;
				return true;
			}
		}
		return false;
	}

	public static boolean checkPassword(int password) 
	{
		if(password==UserClass.store_teacher[flag].getPassword())
		{return true;}
		else
		{return false;}
	}
}
