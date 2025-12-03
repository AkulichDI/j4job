package collectionsAtt;

import java.util.List;

public class AnalyzeByMap {

    public static double averageScore(List<Pupil> pupils) {
        int total = 0;
        int size = 0;
        for (Pupil pupil : pupils){
            for(Subject subject : pupil.subjects()){
                total += subject.score();
                size++;
            }
        }
        return (double) total / size;
    }
    public static List<Label> averageScoreByPupil(List<Pupil> pupils) {
        List<Label> result;

        int total = 0;
        int size = 0;
        for (Pupil pupil : pupils){
            for(Subject subject : pupil.subjects()){
                total += subject.score();
                size++;
            }
        }



        return List.of();
    }
    public static List<Label> averageScoreBySubject(List<Pupil> pupils) {
         return List.of();
    }
    public static Label bestStudent(List<Pupil> pupils) {
        return null;
    }
    public static Label bestSubject(List<Pupil> pupils) {
        return null;
    }


}
