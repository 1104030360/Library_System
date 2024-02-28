/*note1:可以以物件構成ArrayList 
  note2:在程式的最後記得加System.exit(0)
  note3:abstract方法不能宣告static*/
  import javax.swing.*;       
  import java.time.LocalDate;
                                         
  public class UserClass
  {	 
      public static LocalDate d = LocalDate.now(); // 取得今日
      public static LocalDate d2= LocalDate.of(2020, 1, 17); 

      public static Student [] store_student = new Student [20];
      public static Teacher_1 [] store_teacher = new Teacher_1 [20];
      public static Staff_1 [] store_staff=new Staff_1 [20];
      public static int operation1,operation2,operation3,operation4,operation5,operation6;

      public static void main(String[] args) 
      {
          Book.initialize();
          EnterUser();
      }
      
      public static void EnterUser()
      {
          String account_boss,account_employee;
          String account_student,account_teacher,account_staff;
          int password_boss,password_employee,password_student,password_teacher,password_staff;
          int u=-1,y=-1,t=-1;
      
          String username_newAccount;
          int password_newAccount;
      
          String option1[]={"管理員","借閱者"};
      
          String option2_1[]={"館長","館員"};
          String option3_1[]={"決定是否休館","控制員工月薪","編輯書籍"};
          String option3_2[]={"查看月薪","查看今日班表","更改書籍資訊"};
          String option3_3[]={"註冊成會員","使用查書功能"};
      
          String option2_2[]={"以會員身分登入","以非會員身分登入"};
          String option3_4[]={"學生","教師","職員"};
          String option4_1[]={"查看個人資料","查看借還書紀錄","查書與借還書"};
          String option5[]={"回主頁","以會員身分登入"};
          
          Admin a;
          realmember func1 = new realmember();
          
          Boss boss=new Boss();
          Employee emp=new Employee();
          
          
          operation1=JOptionPane.showOptionDialog(null,"歡迎來到圖書館借還系統，請問你是借閱者或管理員","中大圖書館借還系統",1,1,null,option1,null);
          //選擇使用者身分
          if(operation1==0)  //進入管理員
          {
              operation2=JOptionPane.showOptionDialog(null,"請問你是館長還是館員","身分確認",1,1,null,option2_1,null);
              if(operation2==0)  //進入館長
              {
                  do
                  {
                      account_boss=JOptionPane.showInputDialog(null,"請輸入館長帳號","館長驗證I");
                      a=new Boss();
                      if(a.checkAccount(account_boss)==true)  //利用boss內方法檢查帳號:True
                      {
                          password_boss=Integer.parseInt(JOptionPane.showInputDialog(null,"請輸入密碼","館長驗證II"));
                      
                          if(a.checkPassword(password_boss)==true)  //完成兩階段驗證! 進入館長
                          {
                              operation3=JOptionPane.showOptionDialog(null,"館長您好，請點擊你要使用的功能","館長系統",1,1,null,option3_1,null);
                              //館長進入功能選項
                              if(operation3==0)     						    
                                  {boss.Rest();}       //使用功能:是否休館
                              if(operation3==1)     
                                  {boss.Salary();}     //使用功能:控制月薪
                              if(operation3==2)     
                                  {
                                      Admin.adminuser();  //使用功能:編輯書籍
                                      EnterUser();
                                  } 
                          }
                      }
              
                      if(a.checkAccount(account_boss)==false) //利用boss內方法檢查帳號:False
                      {JOptionPane.showMessageDialog(null,"此帳號不是館長帳號! 請再輸一次");}
                  }while(a.checkAccount(account_boss)==false);  //輸入錯誤帳號後可以重複輸入
              }
          
              if(operation2==1)  //進入館員
              {
                  do
                  {
                      account_employee=JOptionPane.showInputDialog(null,"請輸入館員帳號","館員驗證I");
                      a=new Employee();
                      if(a.checkAccount(account_employee)==true)  //利用employee內方法檢查是否有此帳號:True
                      {
                          password_employee=Integer.parseInt(JOptionPane.showInputDialog(null,"請輸入密碼","館員驗證II"));
                      
                          if(a.checkPassword(password_employee)==true)  //確認帳號對應的密碼相同，完成兩階段驗證! 進入館員
                          {
                              operation3=JOptionPane.showOptionDialog(null,"請問今天要使用甚麼功能呢?","館員系統",1,1,null,option3_2,null);
                              //館員進入功能選項
                              if(operation3==0)     //使用功能:查看月薪					    
                                  {emp.checkSalary();}
                              if(operation3==1)     //使用功能:查看今日班表
                                  {emp.checkWork();}
                              if(operation3==2)     //使用功能:編輯書籍
                                  {
                                      Admin.adminuser();
                                      EnterUser();
                                  }
                          }
                      }
              
                      if(a.checkAccount(account_employee)==false) //利用employee內方法檢查帳號:False
                      {JOptionPane.showMessageDialog(null,"此帳號不是館長帳號! 請再輸一次");}
                  }while(a.checkAccount(account_employee)==false);  //輸入錯誤帳號後可以重複輸入
              }
          }
      
          if(operation1==1)  //進入借閱者
          {
              operation2=JOptionPane.showOptionDialog(null,"親愛的用戶您好，請問您是會員嗎?","使用者系統",1,1,null,option2_2,null);
              if(operation2==0)        //選擇『會員』身分
              {
                  operation3=JOptionPane.showOptionDialog(null,"親愛的會員您好，請問你要登入的身分為何?","會員系統",1,1,null,option3_4,null);
                  switch(operation3)   
                  {
                  case 0:  //選擇『學生』身分登入
                      do
                      {
                          account_student=JOptionPane.showInputDialog(null,"請輸入學生會員帳號","會員──學生驗證I");
                          if(Student.checkName(account_student)==true)  //利用student內方法檢查是否有此帳號:True
                          {
                              password_student=Integer.parseInt(JOptionPane.showInputDialog(null,"請輸入密碼","會員──學生驗證II"));
                              if(Student.checkPassword(password_student)==true)  //確認帳號對應的密碼相同，完成兩階段驗證! 進入學生系統
                              {
                                  for(int i=0;i<store_student.length;i++)
                                  {
                                      if(account_student.equals(store_student[i].getName()))
                                      {u=i;}
                                  }
                                  operation4=JOptionPane.showOptionDialog(null,"同學您好，請點擊你要使用的功能","(會員)學生系統",1,1,null,option4_1,null);
                                  //學生進入功能選項
                                  switch(operation4)
                                  {
                                  case 0:  //使用功能:查看個資
                                      store_student[u].checkPersonalInfo();
                                      break;
                                  case 1:  //使用功能:查看還書紀錄
                                      store_student[u].checkBorrowedBook();
                                      break;
                                  case 2:  //使用功能:查書與借還書
                                      Memberborrow();
                                      EnterUser();
                                      break;
                                  }
                              
                              }
                          }
                          if(Student.checkName(account_student)==false)  //利用student內方法檢查是否有此帳號:false
                              {JOptionPane.showMessageDialog(null,"此帳號不存在! 請再輸一次");}       
                      }while(Student.checkName(account_student)==false);
                  break;	
                  case 1:  //選擇『教師』身分登入
                      do
                      {
                          account_teacher=JOptionPane.showInputDialog(null,"請輸入教師會員帳號","會員──教師驗證I");
                          if(Teacher_1.checkName(account_teacher)==true)  //利用teacher內方法檢查是否有此帳號:True
                          {
                              password_teacher=Integer.parseInt(JOptionPane.showInputDialog(null,"請輸入密碼","會員──教師驗證II"));
                              if(Teacher_1.checkPassword(password_teacher)==true)  //確認帳號對應的密碼相同，完成兩階段驗證! 進入教師系統
                              {
                                  for(int b=0;b<store_teacher.length;b++)
                                  {
                                      if(account_teacher.equals(store_teacher[b].getName()))
                                      {y=b;}
                                  }
                                  operation4=JOptionPane.showOptionDialog(null,"老師您好，請點擊你要使用的功能","(會員)教師系統",1,1,null,option4_1,null);
                                  //老師進入功能選項
                                  switch(operation4)
                                  {
                                  case 0:  //使用功能:查看個資
                                      store_teacher[y].checkPersonalInfo();
                                      break;
                                  case 1:  //使用功能:查看還書紀錄
                                      store_teacher[y].checkBorrowedBook();
                                      break;
                                  case 2:  //使用功能:查書與借還書
                                      Memberborrow();
                                      EnterUser();
                                      break;
                                  }
                              }
                          }
                          if(Teacher_1.checkName(account_teacher)==false)  //利用teacher內方法檢查是否有此帳號:false
                          {JOptionPane.showMessageDialog(null,"此帳號不存在! 請再輸一次");}	
                      }while(Student.checkName(account_teacher)==false);
                  break;	
                  case 2:  //選擇『職員』身分登入
                      do
                      {
                          account_staff=JOptionPane.showInputDialog(null,"請輸入職員會員帳號","會員──職員驗證I");
                          if(Staff_1.checkName(account_staff)==true)  //利用staff內方法檢查是否有此帳號:True
                          {
                              password_staff=Integer.parseInt(JOptionPane.showInputDialog(null,"請輸入密碼","會員──職員驗證II"));
                              if(Staff_1.checkPassword(password_staff)==true)  //確認帳號對應的密碼相同，完成兩階段驗證! 進入職員系統
                              {
                                  for(int v=0;v<store_staff.length;v++)
                                  {
                                      if(account_staff.equals(store_staff[v].getName()))
                                      {t=v;}
                                  }
                                  operation4=JOptionPane.showOptionDialog(null,"職員您好，請點擊你要使用的功能","(會員)教師系統",1,1,null,option4_1,null);
                                  //職員進入功能選項
                                  switch(operation4)
                                  {
                                  case 0:  //使用功能:查看個資
                                      store_staff[t].checkPersonalInfo();
                                      break;
                                  case 1:  //使用功能:查看還書紀錄
                                      store_staff[t].checkBorrowedBook();
                                      break;
                                  case 2:  //使用功能:查書與借還書
                                      Memberborrow();
                                      EnterUser();
                                      break;
                                  }
                              }
                          }
                          if(Staff_1.checkName(account_staff)==false)  //利用staff內方法檢查是否有此帳號:false
                              {JOptionPane.showMessageDialog(null,"此帳號不存在! 請再輸一次");}	
                      }while(Staff_1.checkName(account_staff)==false);
                  break;	
                  }
              }
      
              if(operation2==1)  //選擇『非會員』身分
              {
                  operation3=JOptionPane.showOptionDialog(null,"請問要註冊成會員或使用查書功能?","非會員介面",1,1,null,option3_3,null);
                  if(operation3==0)  //進入註冊系統
                  {
                      username_newAccount=JOptionPane.showInputDialog(null,"輸入使用者名稱","註冊系統I");
                      password_newAccount=Integer.parseInt(JOptionPane.showInputDialog(null,"輸入您要的密碼","註冊系統II"));
                      operation4=JOptionPane.showOptionDialog(null,"請問你是哪種身分?","註冊系統III",1,1,null,option3_4,null);
                      switch(operation4)  //將不同身分的人創進不同的物件裡
                      {
                      case 0:  
                          Student stu=new Student(username_newAccount,password_newAccount,"學生",null);
                          Student.storeStudent(stu);
                          JOptionPane.showMessageDialog(null,testStudentArray());  //測試用，可移除
                          operation5=JOptionPane.showOptionDialog(null,"成功創建會員!"+"\n要直接登入嗎?","詢問──使用功能",1,1,null,option5,null);
                          if(operation5==0)
                          {EnterUser();}
                          if(operation5==1)
                          {login_Member();}
                          break;
                      case 1:
                          Teacher_1 teacher=new Teacher_1(username_newAccount,password_newAccount,"教師",null);
                          Teacher_1.storeTeacher(teacher);
                          String array=testTeacherArray();
                          JOptionPane.showMessageDialog(null,array);  //測試用，可移除
                          operation5=JOptionPane.showOptionDialog(null,"成功創建會員!"+"\n要直接登入嗎?","詢問──使用功能",1,1,null,option5,null);
                          if(operation5==0)
                          {EnterUser();}
                          if(operation5==1)
                          {login_Member();}
                          break;
                      case 2:
                          Staff_1 staff=new Staff_1(username_newAccount,password_newAccount,"職員",null);
                          Staff_1.storeStaff(staff);
                          JOptionPane.showMessageDialog(null,testStaffArray());  //測試用，可移除
                          operation5=JOptionPane.showOptionDialog(null,"成功創建會員!"+"\n要直接登入嗎?","詢問──使用功能",1,1,null,option5,null);
                          if(operation5==0)
                          {EnterUser();}
                          if(operation5==1)
                          {login_Member();}
                          break;
                      }
                  }
                  if(operation3==1)  //進入查書系統
                  {func1.function();}
              }
          }
      }
      
  
      public static String testStudentArray()  //測試用方法
      {
          String output="";
      
          for(int i=0;i<store_student.length;i++)
          {
              if(store_student[i]!=null)
                  {output+=store_student[i].getName()+" "+store_student[i].getIdentity()+"\n";}
          }
          return output;
      }
      
      
      public static String testTeacherArray()  //測試用方法
      {
          String output="";
      
          for(int i=0;i<store_teacher.length;i++)
          {
              if(store_teacher[i]!=null)
                  {output+=store_teacher[i].getName()+" "+store_teacher[i].getIdentity()+"\n";}
          }
          return output;
      }
      
      
      public static String testStaffArray()  //測試用方法
      {
          String output="";
      
          for(int i=0;i<store_staff.length;i++)
          {
              if(store_staff[i]!=null)
                  {output+=store_staff[i].getName()+" "+store_staff[i].getIdentity()+"\n";}
          }
          return output;
      }
      
      
      public static void login_Member()
      {
          int operation3,operation4;
          
          String account_student,account_teacher,account_staff;
          int password_student,password_teacher,password_staff;
          
          String option3_4[]={"學生","教師","職員"};
          String option4_1[]={"查看個人資料","查看借還書紀錄","查書與借還書"};
          int u=-1,y=-1,t=-1;
          
          
          operation3=JOptionPane.showOptionDialog(null,"親愛的會員您好，請問你要登入的身分為何?","會員系統",1,1,null,option3_4,null);
          switch(operation3)   
          {
          case 0:  //選擇『學生』身分登入
              do
              {
                  account_student=JOptionPane.showInputDialog(null,"請輸入學生會員帳號","會員──學生驗證I");
                  if(Student.checkName(account_student)==true)  //利用student內方法檢查是否有此帳號:True
                  {
                      password_student=Integer.parseInt(JOptionPane.showInputDialog(null,"請輸入密碼","會員──學生驗證II"));
                      if(Student.checkPassword(password_student)==true)  //確認帳號對應的密碼相同，完成兩階段驗證! 進入學生系統
                      {
                          for(int i=0;i<store_student.length;i++)
                          {
                              System.out.println(store_student[i].getName());
                              String storeName=store_student[i].getName();
                              if(account_student.equals(storeName))
                              {
                                  u=i;
                                  break;
                              }
                          }
                          operation4=JOptionPane.showOptionDialog(null,"同學您好，請點擊你要使用的功能","(會員)學生系統",1,1,null,option4_1,null);
                          //學生進入功能選項
                          switch(operation4)
                          {
                          case 0:  //使用功能:查看個資
                              store_student[u].checkPersonalInfo();
                              break;
                          case 1:  //使用功能:查看還書紀錄
                              store_student[u].checkBorrowedBook();
                              break;
                          case 2:  //使用功能:查書與借還書
                              Memberborrow();
                              break;
                          }
                      
                      }
                  }
                  if(Student.checkName(account_student)==false)  //利用student內方法檢查是否有此帳號:false
                      {JOptionPane.showMessageDialog(null,"此帳號不存在! 請再輸一次");}       
              }while(Student.checkName(account_student)==false);
          break;
          case 1:  //選擇『教師』身分登入
              do
              {
                  account_teacher=JOptionPane.showInputDialog(null,"請輸入教師會員帳號","會員──教師驗證I");
                  if(Teacher_1.checkName(account_teacher)==true)  //利用teacher內方法檢查是否有此帳號:True
                  {
                      password_teacher=Integer.parseInt(JOptionPane.showInputDialog(null,"請輸入密碼","會員──教師驗證II"));
                      if(Teacher_1.checkPassword(password_teacher)==true)  //確認帳號對應的密碼相同，完成兩階段驗證! 進入教師系統
                      {
                          for(int b=0;b<store_teacher.length;b++)
                          {
                              if(account_teacher.equals(store_teacher[b].getName()))
                              {
                                  y=b;
                                  break;
                              }
                          }
                          operation4=JOptionPane.showOptionDialog(null,"老師您好，請點擊你要使用的功能","(會員)教師系統",1,1,null,option4_1,null);
                          //老師進入功能選項
                          switch(operation4)
                          {
                          case 0:  //使用功能:查看個資
                              store_teacher[y].checkPersonalInfo();
                              break;
                          case 1:  //使用功能:查看還書紀錄
                              store_teacher[y].checkBorrowedBook();
                              break;
                          case 2:  //使用功能:查書與借還書
                              Memberborrow();
                              EnterUser();
                              break;
                          }
                      }
                  }
                  if(Teacher_1.checkName(account_teacher)==false)  //利用teacher內方法檢查是否有此帳號:false
                  {JOptionPane.showMessageDialog(null,"此帳號不存在! 請再輸一次");}	
              }while(Student.checkName(account_teacher)==false);
          break;	
          case 2:  //選擇『職員』身分登入
              do
              {
                  account_staff=JOptionPane.showInputDialog(null,"請輸入職員會員帳號","會員──職員驗證I");
                  if(Staff_1.checkName(account_staff)==true)  //利用staff內方法檢查是否有此帳號:True
                  {
                      password_staff=Integer.parseInt(JOptionPane.showInputDialog(null,"請輸入密碼","會員──職員驗證II"));
                      if(Staff_1.checkPassword(password_staff)==true)  //確認帳號對應的密碼相同，完成兩階段驗證! 進入職員系統
                      {
                          for(int v=0;v<store_staff.length;v++)
                          {
                              if(account_staff.equals(store_staff[v].getName()))
                              {
                                  t=v;
                                  break;
                              }
                          }
                          operation4=JOptionPane.showOptionDialog(null,"職員您好，請點擊你要使用的功能","(會員)教師系統",1,1,null,option4_1,null);
                          //職員進入功能選項
                          switch(operation4)
                          {
                          case 0:  //使用功能:查看個資
                              store_staff[t].checkPersonalInfo();
                              break;
                          case 1:  //使用功能:查看還書紀錄
                              store_staff[t].checkBorrowedBook();
                              break;
                          case 2:  //使用功能:查書與借還書
                              Memberborrow();
                              break;
                          }
                      }
                  }
                  if(Staff_1.checkName(account_staff)==false)  //利用staff內方法檢查是否有此帳號:false
                      {JOptionPane.showMessageDialog(null,"此帳號不存在! 請再輸一次");}	
              }while(Staff_1.checkName(account_staff)==false);
          break;	
          }
      }
      public static void Memberborrow()
      {
          int operation1;
          
          String option1[]={"借書","還書","查書"};
          String optionques[]={"id","name"};
          
          String bookID="";
          int bookoption1=-1;
          String bookname="";
          String bookwritter;
          String bookpublisher="";
          int option=-1;
          
          operation1=JOptionPane.showOptionDialog(null,"親愛的會員您好，請問你要查書、借書還是還書","會員借還書系統",1,1,null,option1,null);
          if(operation1==2)  //查書(擷取Adminuser的方法)
          {

              JOptionPane.showMessageDialog(null,"目前館藏有:"+"\n"+Book.getbookname(),"輸出",0);
              operation6=JOptionPane.showOptionDialog(null,"親愛的會員您好，要選擇哪個功能查書","會員借還書系統",JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,null,optionques,optionques[0]);
            if(operation6==0)
            {
              String input4=JOptionPane.showInputDialog(null,"請輸入書的ID","輸入",0);
              Book.setbookname6(operation1,operation6,input4);
              JOptionPane.showMessageDialog(null,Book.returnbook(),"輸出",0);
            }
          
              else{
              String input=JOptionPane.showInputDialog(null,"請輸入書的書名","輸入",0);
              Book.setbookname7(operation1,operation6,input);
              JOptionPane.showMessageDialog(null,Book.returnbook(),"輸出",0);
            }
            }
          
          else if(operation1==0)  //借書
          {
            operation6=JOptionPane.showOptionDialog(null,"親愛的會員您好，要選擇哪個功能借書","會員借還書系統",JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,null,optionques,optionques[0]);
            if(operation6==0){
            String input4=JOptionPane.showInputDialog(null,"請輸入書的ID","輸入",0);
            Book.setbookname3(operation1,operation6,input4);
            JOptionPane.showMessageDialog(null,Book.getbookname3(),"輸出",0);
        }
            else{
            String input=JOptionPane.showInputDialog(null,"請輸入書的書名","輸入",0);
            Book.setbookname4(operation1,operation6,input);
            JOptionPane.showMessageDialog(null,Book.getbookname3(),"輸出",0);
            JOptionPane.showMessageDialog(null,"借書時間是:"+d.toString()+"\n"+"還書日期是:"+d.plusWeeks(2L),"輸出",0);
            
        }
              
          }
          else if(operation1==1)  //還書
          {
            String input4=JOptionPane.showInputDialog(null,"請輸入書的ID","輸入",0);
            String input=JOptionPane.showInputDialog(null,"請輸入書的書名","輸入",0);
            String input2=JOptionPane.showInputDialog(null,"請輸入書的作者姓名","輸入",0);
            String input3=JOptionPane.showInputDialog(null,"請輸入書的出版社名稱","輸入",0);
              Book.setbookname5(operation1,input4,input,input2,input3);
              JOptionPane.showMessageDialog(null,Book.getbookname3(),"輸出",0);
              
          }
      }
      
    }
      
  