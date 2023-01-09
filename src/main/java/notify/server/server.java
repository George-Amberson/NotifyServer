package notify.server;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.Int32Value;
import io.grpc.stub.StreamObserver;
import proto.NotifyServiceGrpc;
import proto.NotifyServiceOuterClass;
import proto.NotifyServiceOuterClass.identificationResponse;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
public class server extends NotifyServiceGrpc.NotifyServiceImplBase {
    private String adress = "jdbc:mysql://localhost:3306/notes";
    private String user = "root";
    private String password = "root";
    private Connection connection;
    private SortedSet<NotifyServiceOuterClass.Note> noteSet = new TreeSet<NotifyServiceOuterClass.Note>(new Comparator<NotifyServiceOuterClass.Note>() {
        @Override
        public int compare(NotifyServiceOuterClass.Note s1, NotifyServiceOuterClass.Note s2) {
            return java.sql.Timestamp.valueOf(LocalDateTime.parse(s1.getDateAndTime())).compareTo(java.sql.Timestamp.valueOf(LocalDateTime.parse(s2.getDateAndTime())));
        }
    });
    private TreeMap<Integer, NotifyServiceOuterClass.Note> noteIdNoteLink = new TreeMap<Integer, NotifyServiceOuterClass.Note>();
    public server() {
        // api call to database
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(adress, user, password);
            System.out.println("Database connection successful " + adress);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM allnotes");
            while(resultSet.next()){
                NotifyServiceOuterClass.Note note = NotifyServiceOuterClass.Note.newBuilder().setId(resultSet.getInt(1))
                        .setTextInfo(resultSet.getString(2))
                        .setDateAndTime(resultSet.getString(3)).build();
                noteSet.add(note);
                noteIdNoteLink.put(note.getId(), note);
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getALlNotes(NotifyServiceOuterClass.identificationRequest request,
                     StreamObserver<identificationResponse> responseObserver){
        System.out.println("get all Notes");
        System.out.println(noteSet);
        NotifyServiceOuterClass.identificationResponse response = NotifyServiceOuterClass.identificationResponse.newBuilder()
                .addAllNotes(noteSet)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    @Override
    public void addNewNote(NotifyServiceOuterClass.Note noteRequest, StreamObserver<BoolValue> responseObserver){
        System.out.println("ADD NEw Note");
        noteIdNoteLink.put(noteRequest.getId(), noteRequest);
        try {
            Statement statement = connection.createStatement();
            String sqlRequest = "INSERT allnotes(id, textInfo, dateAndTime) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlRequest);
            preparedStatement.setString(1, String.valueOf(noteRequest.getId()));
            preparedStatement.setString(2, noteRequest.getTextInfo());
            preparedStatement.setString(3, noteRequest.getDateAndTime());
            preparedStatement.executeUpdate();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
        responseObserver.onNext(BoolValue.of(noteSet.add(noteRequest)));
        responseObserver.onCompleted();
    }
    @Override
    public void deleteNote(Int32Value noteId, StreamObserver<BoolValue> responseObserver){
        System.out.println("delete all note");
        if(noteIdNoteLink.containsKey(noteId.getValue())){
            noteSet.remove(noteIdNoteLink.get(noteId.getValue()));
            noteIdNoteLink.remove(noteId.getValue());
            responseObserver.onNext(BoolValue.of(true));
                try {
                    Statement statement = connection.createStatement();
                    int rows = statement.executeUpdate("DELETE FROM allnotes WHERE Id = "+ noteId.getValue());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }


        }else{
            responseObserver.onNext(BoolValue.of(false));
        }
        responseObserver.onCompleted();
    }
    @Override
    public void getTopNote(Empty request, StreamObserver<NotifyServiceOuterClass.Note> response) {
       if(!noteSet.isEmpty()) {
           response.onNext(noteSet.first());
       }else{
            response.onNext(NotifyServiceOuterClass.Note.newBuilder().setTextInfo("Sorry no new Notes").build());
       }
       response.onCompleted();
    }
}
