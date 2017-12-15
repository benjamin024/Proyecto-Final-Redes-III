package WS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class Conexion {
    private Connection con;
    private Statement stmt;
    private String server;
    private String bd;
    private String user;
    private String psw;
    
    public Conexion(String server, String bd, String user, String psw){
        this.server = server;
        this.bd = bd;
        this.user = user;
        this.psw = psw;
    }

    
    public Connection getCon() {
        return con;
    }

    
    public void setCon(Connection con) {
        this.con = con;
    }

    
    public Statement getStmt() {
        return stmt;
    }

    
    public void setStmt(Statement stmt) {
        this.stmt = stmt;
    }
    
    public boolean conectar(){
        String url="jdbc:mysql://"+server+":3306/"+bd;
        try{
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection con=DriverManager.getConnection(url,user,psw);
            stmt = con.createStatement();
            return true;
        }catch(SQLException e){
           System.out.println("Exception SQL en conectar(): ");
           System.out.println("Mensaje : " + e.getMessage());
           System.out.println("SQLState : " + e.getSQLState());
           System.out.println("Código proveedor : " + e.getErrorCode());
           e.printStackTrace();
           return false;
        }catch(Exception ex){
            System.out.println("Se ha lanzado una Exception no SQL en conectar(): ");
            ex.printStackTrace();
            return false;
        }
    }
    
    public boolean abc(String sql){
        try{
            getStmt().executeUpdate(sql);
            return true;
        }catch(SQLException e){
           System.out.println("Exception SQL en abc(String): ");
           System.out.println("Mensaje : " + e.getMessage());
           System.out.println("SQLState : " + e.getSQLState());
           System.out.println("Código proveedor : " + e.getErrorCode());
           e.printStackTrace();
           return false;
        }catch(Exception ex){
            System.out.println("Se ha lanzado una Exception no SQL en abc(String): ");
            ex.printStackTrace();
            return false;
        }
    }
    
    public void cerrar(){
        try{
            getStmt().close();
            getCon().close();
        }catch(SQLException e){
           System.out.println("Exception SQL : ");
           System.out.println("Mensaje : " + e.getMessage());
           System.out.println("SQLState : " + e.getSQLState());
           System.out.println("Código proveedor : " + e.getErrorCode());
           e.printStackTrace();
        }catch(Exception ex){
            System.out.println("Se ha lanzado una Exception no SQL : ");
            ex.printStackTrace();
        }
        
    }
    
    public ResultSet consulta (String sql) {
    	ResultSet resultado = null;
    	try {
    		resultado = getStmt().executeQuery(sql);
    	}catch(Exception e) {
    		System.out.println(e.toString());
    	}
    	return resultado;
    }
}
