public class AdminUser
{
	public String username;
	public int  password;
	public String identity;
    	
    public AdminUser(String name,int password,String identity)
    {
    	setName(name);
    	setPassword(password);
    	setIdentity(identity);
    }
    
    public void setName(String name)
    {username=name;}
    
    public String getName()
    {return username;}
    
    public void setPassword(int pass)
    {password=pass;}
    
    public int getPassword()
    {return password;}
    
    public void setIdentity(String iden)
    {identity=iden;}
    
    public String getIdentity()
    {return identity;}
}