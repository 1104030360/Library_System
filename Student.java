public class Student extends Member_1
{
	public Book [] Borrowed_personal = new Book [50];  //學生的個人借書庫
	String personaldata="";
	static int flag=-1;
	
	public Student(String name,int password,String identity,Book [] personalBook)
	{
		super(name,password,identity);
		
		Borrowed_personal=personalBook;
	}
	
	public static void storeStudent(Student stu)
	{
		for(int i=0;i<=UserClass.store_student.length;i++)
		{
			if(UserClass.store_student[i]==null)
			{
				UserClass.store_student[i]=stu;
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
		String dig=Integer.toString(getPassword());
		String length_password="";
		for(int i=1;i<=Integer.parseInt(dig);i++)
		{length_password+="*";}
		return personaldata+="使用者名稱:"+getName()+"\n密碼:"+length_password+"\n身分別:"+getIdentity();
	}

	
	public static boolean checkName(String Name) 
	{
		for(int i=0;i<UserClass.store_student.length;i++)
		{
			String getName=UserClass.store_student[i].getName();
			if(Name.equals(getName))
			{
				flag=i;
				return true;
			}
		}
		return false;
	}

	public static boolean checkPassword(int password) 
	{
		if(password==UserClass.store_student[flag].getPassword())
		{return true;}
		else
		{return false;}
	}
}

