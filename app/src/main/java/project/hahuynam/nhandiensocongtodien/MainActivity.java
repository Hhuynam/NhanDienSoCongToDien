package project.hahuynam.nhandiensocongtodien;
import androidx.appcompat.app.AppCompatActivity; //cung cấp appbar,theme,...
import android.os.Bundle;//cho OnCreate()-giúp khôi phục trạng thái app trước đó khi thoát
//main_activity=chương trình chính; kế thừa từ lớp AppCompatActivity để quản lý giao diện,app bar
public class MainActivity extends AppCompatActivity{
    //Phương thức khởi tạo đầu tiên OnCreate(), khai báo ghi đè OnCreate() sẵn có trong lớp cha-AppCompatActivity
    @Override
    protected void onCreate(Bundle savedInstanceState){
    /*
    protected cho phép truy cập lớp hiện tại+các lớp con ở package khác;
     */
    super.onCreate(savedInstanceState); //khởi tạo trạng thái cơ bản của activity
    setContentView(R.layout.layout); //kết nối activity với file xml giao diện
    }
}
