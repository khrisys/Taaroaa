package fr.drochon.christian.taaroaa.course;

import android.graphics.drawable.GradientDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.model.Course;

/**
 * Classe responsable de chaque ligne de la recyclerview, et donc, chaque cours
 */
public class PupilsViewHolder extends RecyclerView.ViewHolder{


    // ROOT
    LinearLayout mLinearLayoutRoot;
    // ELEMENTS GRAPHIQUES
    //@BindView(R.id.scrollviewRecyclerView) ScrollView mScrollView;
    //@BindView(R.id.calendrier_eleves) CalendarView mCalendarView;
    RecyclerView mRecyclerViewCoursesPupils;
    // FLOATINGBUTTON
    //@BindView(R.id.fab) FloatingActionButton mFloatingActionButton;

    // CELLULES
    Sixth<String, String, String, String, String, Date> currentSixth;
    //private TextView mCourseType;
    @BindView(R.id.liste_cell_linear_layout) LinearLayout mLinearLayout;
    @BindView(R.id.list_cell_course_subject) TextView mCourseSubject;
    @BindView(R.id.liste_cell_course_type) TextView mCourseType;
    //private TextView mCoursesubject;

    /**
     * Contructeur qui prend en param la vue affichée.
     * Je recupere les 2 textview.
     * responsable du clic sur les cellules.
     *
     * @param itemView : cellule d'une liste comprenant le titre et la description d'un cours
     */
    public PupilsViewHolder(final View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        mRecyclerViewCoursesPupils = itemView.findViewById(R.id.recyclerViewCoursesPupils);


        //mCourseType = itemView.findViewById(R.id.list_cell_course_type);
        //mCoursesubject = itemView.findViewById(R.id.course_subject);
/*            mCourseSupervisor = itemView.findViewById(R.id.course_supervisor);
            mCourseLevel = itemView.findViewById(R.id.course_level);
            mCourseDate = itemView.findViewById(R.id.course_date);
            mCourseHeure = itemView.findViewById(R.id.course_heure);*/

        // Affichage de la notification
        // 2 : j'utilise l'ecouteur sur la cellule et recupere les informations pour les affihcer dans une notification
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s;
                s = currentSixth.second + "\nNiveau " + currentSixth.third + "\n" + currentSixth.fourth + "\n" + currentSixth.fifth + "\n" + currentSixth.sixth;
                new AlertDialog.Builder(itemView.getContext())
                        .setTitle(currentSixth.first)
                        .setMessage(s)
                        .show();
            }
        });
    }

    void display() {
        display();
    }

    /**
     * affichage de la sixth qui est fournie, soit leRecy nom et la mCoursesubject avec un String generique pour chacun d'entre eux
     *
     * @param sixth : variable composée des 6 elements d'un cours
     */
    void display(Sixth<String, String, String , String, String, Date> sixth) {
        // 1 : enregistrer la sixth que je clique + nom et mCoursesubject
        currentSixth = sixth;

        mCourseType.setText(currentSixth.first);
        mCourseSubject.setText(currentSixth.second);
                /* mCourseSupervisor.setText(sixth.third);
            mCourseLevel.setText(sixth.fourth);
            mCourseDate.setText(sixth.fifth);
            mCourseHeure.setText(sixth.sixth);*/

    }

    /**
     * 4
     * Methode appellée via l'adapter. Cette methode mettra à jour les differentes view du viewholder en fonction d'un objet course passé en param
     * Appelle la methode updateDesignDependingUser permettant de changer la position à l'ecran des vues attachées (donc => les messages)
     * grace à la mise à jour de leurs param de layout
     * @param course
     */
    public void updateWithCourse(Course course){

        // Check if current user is the sender
        //Boolean isCurrentUser = course.getUserSender().getUid().equals(currentUserId);

        // Update course TextView
        this.mCourseType.setText(course.getTypeCours());
        this.mCourseSubject.setText(course.getSujetDuCours());
        //this.textViewMessage.setTextAlignment(isCurrentUser ? View.TEXT_ALIGNMENT_TEXT_END : View.TEXT_ALIGNMENT_TEXT_START);

        // Update date TextView
        //if (course.getDateCreated() != null) this.textViewDate.setText(this.convertDateToHour(course.getDateCreated()));

        // Update isMentor ImageView
        //this.imageViewIsMentor.setVisibility(course.getUserSender().getIsMentor() ? View.VISIBLE : View.INVISIBLE);

        // Update profile picture ImageView
/*
        if (course.getUserSender().getUrlPicture() != null)
            glide.load(course.getUserSender().getUrlPicture())
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageViewProfile);
*/

        // Update image sent ImageView
/*        if (course.getUrlImage() != null){
            glide.load(course.getUrlImage())
                    .into(imageViewSent);
            this.imageViewSent.setVisibility(View.VISIBLE);
        } else {
            this.imageViewSent.setVisibility(View.GONE);
        }*/

        //Update Message Bubble Color Background
        //((GradientDrawable) textMessageContainer.getBackground()).setColor(isCurrentUser ? colorCurrentUser : colorRemoteUser);

        // Update all views alignment depending is current user or not
        //this.updateDesignDependingUser(isCurrentUser);
    }

/*    *//**
     * 5
     * Methode permettant de changer la position à l'ecran des vues attachées (donc => les messages)
     * grace à la mise à jour de leurs param de layout : à droite les messages qu'on a envoyé et à gauche , les messages recus
     * @param isSender
     *//*
    private void updateDesignDependingUser(Boolean isSender){

        // PROFILE CONTAINER
        RelativeLayout.LayoutParams paramsLayoutHeader = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsLayoutHeader.addRule(isSender ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.ALIGN_PARENT_LEFT);
        this.profileContainer.setLayoutParams(paramsLayoutHeader);

        // MESSAGE CONTAINER
        RelativeLayout.LayoutParams paramsLayoutContent = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsLayoutContent.addRule(isSender ? RelativeLayout.LEFT_OF : RelativeLayout.RIGHT_OF, R.id.activity_mentor_chat_item_profile_container);
        this.messageContainer.setLayoutParams(paramsLayoutContent);

        // CARDVIEW IMAGE SEND
        RelativeLayout.LayoutParams paramsImageView = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsImageView.addRule(isSender ? RelativeLayout.ALIGN_LEFT : RelativeLayout.ALIGN_RIGHT, R.id.activity_mentor_chat_item_message_container_text_message_container);
        this.cardViewImageSent.setLayoutParams(paramsImageView);

        this.rootView.requestLayout();
    }*/

    // ---

/*    private String convertDateToHour(Date date){
        DateFormat dfTime = new SimpleDateFormat("HH:mm");
        return dfTime.format(date);
    }*/
}
