<!DOCTYPE html>
<html>
  <head>
    <style>
       #map {
        height: 640px;
        width: 100%;
       }
       .select-style {
            border: 2px solid #000;
            width: 250px;
            height: 40px;
            border-radius: 10px;
            overflow: hidden;
            color: #000;
            font-size: 16px;
            text-align: center;
            font-weight: bold;
            background: #fafafa;
        }

        .button-style{
            border: 2px solid #013F8F;
            height: 40px;
            border-radius: 10px;
            overflow: hidden;
            color: #fff;
            font-size: 16px;
            text-align: center;
            font-weight: bold;
            background: #009BFF;
        }

        .select-style select {
            padding: 5px 8px;
            width: 130%;
            border: none;
            box-shadow: none;
            background: transparent;
            background-image: none;
            -webkit-appearance: none;
        }

        .select-style select:focus {
            outline: none;
        }
    </style>
    <title>Proyecto Redes de Computadoras III</title>
  </head>
  <body>
    <div style="position: absolute; width: 40%; left: 30%; z-index: 4;"><center>
       <form action="index.php" method="get"><br>
            <select name="r" class="select-style">
                <option value="20">Ruta 20</option>
                <option value="40">Ruta 40</option>
            </select>
            <input type="submit" class="button-style" value="Aceptar"/>
       </form>
    </center></div>
    <div id="map"></div>
    <script>
    <?php 
        require("conexion.php");
        if(empty($_GET["r"]))
            $ruta = "20";
        else
            $ruta = $_GET["r"];
        $sql = "SELECT lat, lng FROM estacion WHERE id = (SELECT MIN(id) FROM estacion WHERE r='".$ruta."');";
        $consulta = $conn->query($sql);
        while($row = $consulta->fetch_assoc()) {
            $lat = $row["lat"];
            $lng = $row["lng"];
        }
      ?>
      function initMap() {
        var lat = <?php echo $lat; ?>;
        var lng = <?php echo $lng; ?>;
        var map = new google.maps.Map(document.getElementById('map'), {
          zoom: 15,
          center: new google.maps.LatLng(lat, lng)
        });
        <?php
            $sql = "SELECT * FROM estacion WHERE r='".$ruta."';";
            $consulta = $conn->query($sql);
            $i = 0;
            while($row = $consulta->fetch_assoc()) {
                
        ?>
                var lat = <?php echo $row["lat"]; ?>;
                var lng = <?php echo $row["lng"]; ?>;
                var img = "st.png";
                var <?php echo "marker$i"; ?> = new google.maps.Marker({
                    position: new google.maps.LatLng(lat, lng),
                    map: map,
                    title: <?php echo "'Estación ".$row["id"]."'"; ?>,
                    icon: img
                });     
        <?php
                $sql = "SELECT * FROM checkin WHERE estacion = ".$row["id"]." ORDER BY hora DESC;";
                $info  = "<div id='content'><h3>Estación ".$row["id"]."</h3><br>";
                $info .= "<h4><b>Últimos Registros: </b></h4>";
                $cons = $conn->query($sql);
                if($cons->num_rows <= 0)
                $info .= "<center>No hay registros en esta estación</center>";
                else{
                    $info .= "<ul>";
                    while($r = $cons->fetch_assoc()){
                        $info .= "<li>El camión <b>".$r["camion"]."</b> registró una visita aquí el <b>".$r["hora"]."</b>.</li>";
                    }
                    $info .= "</ul>";
                }
        ?>
                var contentString = "<?php echo $info; ?>";
                var <?php echo "infowindow$i"; ?> = new google.maps.InfoWindow({
                    content: contentString
                });
                <?php echo "marker$i"; ?>.addListener('click', function() {
                    <?php echo "infowindow$i"; ?>.open(map, <?php echo "marker$i"; ?>);
                });
        <?php
            $i++;
            }
        ?>
      }
    </script>
    <script async defer
    src="https://maps.googleapis.com/maps/api/js?key=AIzaSyAPqV0gAAobGyUg2xoa2wNhIELMYqDLcuk&callback=initMap">
    </script>
  </body>
</html>