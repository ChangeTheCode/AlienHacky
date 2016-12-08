package at.fhv.alienserver.game;

import at.fhv.alienserver.Common.CoordinateContainer;

import java.util.ArrayList;

/**
 * Created by Jim on 08.12.2016.
 */
public class Logic {
    private static Logic ourInstance = new Logic();

    public static Logic getInstance() {
        return ourInstance;
    }

    private Logic() {

    }

    /*
    * Members
    * */

    private int _score = 0;
    private boolean _rule_is_broken = false;

    private CoordinateContainer _left_top_ege = null;
    private CoordinateContainer _right_top_ege = null;
    private CoordinateContainer _left_bottom_ege = null;
    private CoordinateContainer _right_bottom_ege = null;


    /*
    * Public function
    * */
    public boolean set_border_values(ArrayList<CoordinateContainer> eges){
        if(eges.size() != 4 ) {
            System.out.println("Error: You don't define all eges of the boarder");
            return false;
        }else{
            for (int i = 0; i< eges.size(); i++){
                switch (eges.get(i).get_name()){
                    case "left_top_corner":
                        _left_top_ege = eges.get(i);
                        break;
                    case "left_bottom_corner":
                        _left_top_ege = eges.get(i);
                        break;
                    case "right_top_corner":
                        _right_top_ege = eges.get(i);
                        break;
                    case "right_bottom_corner":
                        _right_bottom_ege = eges.get(i);
                        break;
                }
            }
            return true;
        }
    }

    public boolean still_inside_border(){
        /*TODo wie prüfen wir ob er noch im Rahmen des Spielfeld ist, wenn er ihn nach aussen schicken soll um schnell genug
            zu fahren. Über die Winkel vom Head finde ich nicht gut !

            wir könnten es so machen, wenn man dem Moving head die Ränder sagt das er dann die winkel / coordinaten zurück gibt die max sind um
            um immer noch ca. im feld zu sein. Da der berechner uns ja nur ein wert gibt der ja im Feld ist. Da wir gesagt haben
            die Grenzen ja das abbruch kriterium seinen.
        */
        return false;
    }



}
