/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WS;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;

/**
 *
 * @author benja
 */
@WebService(serviceName = "DistribuidosWS")
public class DistribuidosWS {

   /**
     * Web service operation
     */
    @WebMethod(operationName = "checkIn")
    public String checkIn(@WebParam(name = "placas") String placas, @WebParam(name = "ruta") String ruta, @WebParam(name = "estacion") String estacion) {
        Locale l = new Locale("es","MX");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Mexico_City"));
        String datetime = "";
        datetime = (cal.get(Calendar.YEAR) + "-" + String.format("%02d",cal.get(Calendar.MONTH)) + "-" + String.format("%02d",cal.get(Calendar.DAY_OF_MONTH)));
        datetime += " " + (String.format("%02d",cal.get(Calendar.HOUR))+":"+String.format("%02d",cal.get(Calendar.MINUTE))+":"+String.format("%02d",cal.get(Calendar.SECOND)));
        System.out.println(datetime);
        Conexion bd = new Conexion("localhost", "distribuidos", "root", "root");
        bd.conectar();
        String sql = "SELECT MAX(hora) as hora FROM checkin WHERE camion ='"+placas+"' AND ruta = '" + ruta + "' AND estacion = " + estacion + ";";
        ResultSet rs = bd.consulta(sql);
        String horaMax = null;
        try {
            rs.next();
            horaMax = rs.getString("hora");
            System.out.println(horaMax);
        } catch (SQLException ex) {
            Logger.getLogger(DistribuidosWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        int minutes = 0;
        if(horaMax != null){
            horaMax = horaMax.split(" ")[1];
            String auxDT = datetime.split(" ")[1];
            LocalTime max = LocalTime.parse(horaMax);
            LocalTime act = LocalTime.parse(auxDT);
            minutes = (int) ChronoUnit.MINUTES.between(max, act);
            System.out.println(minutes);
        }
        if(horaMax == null || minutes > 3){
            sql = "INSERT INTO checkin(camion, ruta, estacion, hora) VALUES('"+placas+"','"+ruta+"',"+estacion+",'"+datetime+"');";
            System.out.println(sql);
            if(bd.abc(sql))
                return "OK";
            else
                return "NO";
            }
        else 
            return "OK";
    }
    
    
    /**
     * Web service operation
     */
    @WebMethod(operationName = "getStations")
    public String getStations(@WebParam(name = "route") String route) {
        System.out.println("GET STATIONS :) Route= "+route);
        Conexion bd = new Conexion("localhost", "distribuidos", "root", "root");
        bd.conectar();
        String sql = "SELECT * FROM estacion WHERE r='"+route+"';";
        String sxml = "";
        ResultSet rs = bd.consulta(sql);
        LinkedList<String> auxL = new LinkedList<String>();
        try {
            
            while(rs.next()){
                String auxS = "{\"id\":"+rs.getString("id")+", \"latitud\":"+rs.getString("lat")+", \"longitud\":"+rs.getString("lng")+"}";
                auxL.add(auxS);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DistribuidosWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        sxml += "{\"estaciones\": [";
        String coma = ",";
        for(int i = 0; i < auxL.size(); i++){
            if(i == auxL.size() - 1)
                coma = "";
            sxml += auxL.get(i) + coma;
        }
        sxml += "]}";
        System.out.println(sxml);
        return sxml;
    }
}
