package notify.server;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.Int32Value;
import io.grpc.stub.StreamObserver;
import proto.NotifyServiceGrpc;
import proto.NotifyServiceOuterClass;
import proto.NotifyServiceOuterClass.identificationResponse;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
public class server extends NotifyServiceGrpc.NotifyServiceImplBase {

    private SortedSet<NotifyServiceOuterClass.Note> noteSet = new TreeSet<NotifyServiceOuterClass.Note>(new Comparator<NotifyServiceOuterClass.Note>() {
        @Override
        public int compare(NotifyServiceOuterClass.Note s1, NotifyServiceOuterClass.Note s2) {
            return java.sql.Timestamp.valueOf(LocalDateTime.parse(s2.getDateAndTime())).compareTo(java.sql.Timestamp.valueOf(LocalDateTime.parse(s1.getDateAndTime())));
        }
    });
    private TreeMap<Integer, NotifyServiceOuterClass.Note> noteIdNoteLink = new TreeMap<Integer, NotifyServiceOuterClass.Note>();
    public server() {
        // api call to database
    }

    @Override
    public void getALlNotes(NotifyServiceOuterClass.identificationRequest request,
                     StreamObserver<identificationResponse> responseObserver){
        NotifyServiceOuterClass.identificationResponse response = NotifyServiceOuterClass.identificationResponse.newBuilder()
                .addAllNotes(noteSet)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    @Override
    public void addNewNote(NotifyServiceOuterClass.Note noteRequest, StreamObserver<BoolValue> responseObserver){
        noteIdNoteLink.put(noteRequest.getId(), noteRequest);
        responseObserver.onNext(BoolValue.of(noteSet.add(noteRequest)));
        responseObserver.onCompleted();
    }
    @Override
    public void deleteNote(Int32Value noteId, StreamObserver<BoolValue> responseObserver){
        if(noteIdNoteLink.containsKey(noteId.getValue())){
            noteSet.remove(noteIdNoteLink.get(noteId.getValue()));
            noteIdNoteLink.remove(noteId.getValue());
            responseObserver.onNext(BoolValue.of(true));
        }else{
            responseObserver.onNext(BoolValue.of(false));
        }
        responseObserver.onCompleted();
    }
}
