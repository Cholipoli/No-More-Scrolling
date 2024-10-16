package com.example.nomorescrolling

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.nomorescrolling.data.ClickEvent
import com.example.nomorescrolling.data.ClickEventDao
import com.example.nomorescrolling.data.ClickEventDatabase
import com.example.nomorescrolling.data.ScrollEvent
import com.example.nomorescrolling.data.ScrollEventDao
import com.example.nomorescrolling.data.ScrollEventDatabase
import com.example.nomorescrolling.data.ScrollSession
import com.example.nomorescrolling.data.ScrollSessionDao
import com.example.nomorescrolling.data.ScrollSessionDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.nomorescrolling.data.KeyboardEvent
import com.example.nomorescrolling.data.KeyboardEventDao
import com.example.nomorescrolling.data.KeyboardEventDatabase
import com.example.nomorescrolling.data.SessionForAI
import android.provider.Settings
import android.net.Uri
import android.view.WindowManager
import android.view.Gravity  // For window gravity alignment
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.database.database


class MyAccessibilityService<Button : View?> : AccessibilityService() {
    private lateinit var scrollEventDatabase: ScrollEventDatabase
    private lateinit var scrollEventDao: ScrollEventDao
    private lateinit var scrollSessionDatabase: ScrollSessionDatabase
    private lateinit var scrollSessionDao: ScrollSessionDao
    private lateinit var clickEventDatabase: ClickEventDatabase
    private lateinit var clickEventDao: ClickEventDao
    private lateinit var keyboardEventDatabase: KeyboardEventDatabase
    private lateinit var keyboardEventDao: KeyboardEventDao
    var riskApps = listOf(
        "com.instagram.android",
        "com.facebook.katana",
        "com.tiktok.android",
        "com.twitter.android",
        "com.google.android.youtube",
        "com.netflix.mediaclient",
        "com.reddit.frontpage"
    )

    override fun onCreate() {
        super.onCreate()


        // Create the notification channel
        createNotificationChannel()

        scrollEventDatabase = ScrollEventDatabase.getDatabase(applicationContext)
        scrollEventDao = scrollEventDatabase.scrollEventDao() // Initialiser l'instance de ScrollEventDao
        scrollSessionDatabase = ScrollSessionDatabase.getDatabase(applicationContext)
        scrollSessionDao = scrollSessionDatabase.scrollSessionDao() // Initialiser l'instance de ScrollEventDao
        clickEventDatabase = ClickEventDatabase.getDatabase(applicationContext)
        clickEventDao = clickEventDatabase.clickEventDao()
        keyboardEventDatabase = KeyboardEventDatabase.getDatabase(applicationContext)
        keyboardEventDao = keyboardEventDatabase.keyboardEventDao()

        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager


    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event != null && event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {

            val timestamp = System.currentTimeMillis()
            val scrollEvent = ScrollEvent(timestamp = timestamp)


            // Obtenir le nom du package de l'application active
            val activePackage = event.packageName?.toString()
            scrollEvent.packageName = activePackage ?: "unknown" // Ajoute cette information à ton ScrollEvent

            // Stocker l'événement de scroll dans la base de données
            insertScrollEvent(scrollEvent)

            // Log pour vérification
            Log.d("MyAccessibilityService", "Scroll detected: at $timestamp")
        }
        if(event != null && event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            val timestamp = System.currentTimeMillis()
            val clickEvent = ClickEvent(timestamp = timestamp)
            insertClickEvent(clickEvent)
            Log.d("DatabaseCheck", "click")

        }
        if(event != null && event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED){
            //we are only looking for keyboard interaction. We are not looking at what the user is typing.
            val timestamp = System.currentTimeMillis()
            val keyboardEvent = KeyboardEvent(timestamp = timestamp)
            insertKeyboardEvent(keyboardEvent)
            Log.d("DatabaseCheck", "type!")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "my_channel_id"
            val channelName = "My Channel"
            val channelDescription = "Channel description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, title: String, message: String) {
        val channelId = "my_channel_id" // Use the same channel ID as created before
        val notificationId = 1 // Unique ID for each notification

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Replace with your notification icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true) // Automatically dismiss the notification when clicked

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    private fun insertClickEvent(clickEvent: ClickEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            clickEventDao.insert(clickEvent)
        }
    }
    private fun insertKeyboardEvent(keyboardEvent: KeyboardEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            keyboardEventDao.insert(keyboardEvent)
        }
    }

    // this function is used for now, until we develop the AI
    fun isDumbScrolling(scrollEvents: List<ScrollEvent>, types: List<KeyboardEvent>): Boolean {
        // Seuils ajustables
        val minIntervalForDumbScroll = 5000L // 5 secondes (pour tenir compte de vidéos plus longues)
        val rapidScrollThreshold = 10 // Nombre de scrolls rapides consécutifs
        var rapidScrollCount = 0

        // Vérifier la régularité des scrolls
        var totalInterval = 0L
        var irregularScrolls = 0
        for (i in 1 until scrollEvents.size) {
            val interval = scrollEvents[i].timestamp - scrollEvents[i - 1].timestamp
            totalInterval += interval

            // Vérifier si le scroll est dans une application à risque
            if (scrollEvents[i].packageName in riskApps) {
                // Si l'intervalle est inférieur au minimum pour un scroll normal (ex: 5 secondes pour des vidéos)
                if (interval < minIntervalForDumbScroll) {
                    rapidScrollCount++
                }
                // Si l'intervalle est très irrégulier, augmenter le compteur
                if (i > 1 && Math.abs(interval - (scrollEvents[i - 1].timestamp - scrollEvents[i - 2].timestamp)) > 3000L) {
                    irregularScrolls++
                }
            }
        }


        // Moyenne des écarts entre scrolls
        val avgInterval = totalInterval / (scrollEvents.size - 1)

        var isDump = rapidScrollCount >= rapidScrollThreshold || irregularScrolls > scrollEvents.size / 2 || avgInterval < minIntervalForDumbScroll
        CoroutineScope(Dispatchers.IO).launch {
            if (clickEventDao.getClicksAfterTimestamp(scrollEvents[0].timestamp).size > scrollEvents.size){
                isDump = false
            }
        }
        Log.d("DatabaseCheck", "${types.size}")
        if (scrollEvents.size < types.size){
            isDump = false
            Log.d("DatabaseCheck", "${types.size}")
        }
        // Statistiques de régularité : si les scrolls sont très irréguliers ou trop rapides
        return isDump
    }

    // this function is used for now, until we develop the AI
    private fun lastSession(scrollSession: List<ScrollEvent>, types: List<KeyboardEvent>): ScrollSession{
        val count = scrollSession.size
        var sessionDuration: Long
        var averageTimeBetweenScrolls: Float?
        if (count > 1) {
            sessionDuration = scrollSession[count - 1].timestamp - scrollSession[0].timestamp
            averageTimeBetweenScrolls = sessionDuration / (count - 1).toFloat() // Corrigé pour les intervalles

            // Affichage des résultats ou traitement des données
            Log.d("ScrollStats", "Session Duration: $sessionDuration ms")
            Log.d("ScrollStats", "Average Time Between Scrolls: $averageTimeBetweenScrolls ms")

            // Ici, tu peux ajouter le code pour enregistrer ces statistiques ou les utiliser comme tu le souhaites
        } else if (count == 1) {
            // Cas où il n'y a qu'un seul événement de scroll
            sessionDuration = 0
            averageTimeBetweenScrolls = null
            Log.d("ScrollStats", "Only one scroll event recorded. Cannot calculate average time.")
        } else {
            // Cas où il n'y a aucun événement de scroll
            sessionDuration = 0
            averageTimeBetweenScrolls = null
            Log.d("ScrollStats", "No scroll events recorded.")
        }
        var numberOfScrollsInRiskApp = 0
        var appUsedAndNumber = mutableMapOf <String, Int>()
        for (scroll in scrollSession ) {
            if (scroll.packageName in riskApps) {
                if (appUsedAndNumber[scroll.packageName] == null){
                    appUsedAndNumber[scroll.packageName] = 1
                }
                else{
                    appUsedAndNumber[scroll.packageName] = appUsedAndNumber[scroll.packageName]!! + 1
                }
                numberOfScrollsInRiskApp++
            }
        }
        val appUsedAndPercent = mutableMapOf <String, Float>()
        for ((key, value) in appUsedAndNumber) {
            appUsedAndPercent[key] = value.toFloat() / count.toFloat()
        }

        val isABadSession = isDumbScrolling(scrollSession, types)


        return ScrollSession(numberOfScrolls = count, sessionDurationInMil = sessionDuration, averageTimeBetweenScrolls = averageTimeBetweenScrolls, appUsed = appUsedAndPercent, isDumpScroll = isABadSession)
    }

    private fun insertScrollEvent(scrollEvent: ScrollEvent) {


        // si scrollevent-sharedpref trop court
            //Shared preference = scrollEvent
        // si assez long
            // insert
            //shared pref = scrollEvent
        /////ceci filtre les scroll en doublon

        CoroutineScope(Dispatchers.IO).launch {
            val sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            val value = sharedPreferences.getLong("last_timestamp", -1)

            if (value == (-1).toLong() || scrollEvent.timestamp - value > 300) {
                Log.d("DatabaseCheck", "New shared pref : ${scrollEvent.timestamp - value}, 1")


                //logique pour savoir si début de scroll ou pas !!!!!!!!!!!!!!!!! changer les 15 sec en 3 minutes
                val lastScroll = scrollEventDao.getLastScrollEvent()
                if(lastScroll != null) {
                    Log.d("DatabaseCheck", (scrollEvent.timestamp - lastScroll.timestamp).toString())
                    if (scrollEvent.timestamp - lastScroll.timestamp >= 3 * 60 * 1000) {
                        scrollEvent.isAScrollSessionStart = true
                        //faux

                        scrollEventDao.insert(scrollEvent)
                        Log.d("DatabaseCheck", "New event added : ${scrollEventDao.getLastScrollEvent()}")

                        //make stats about last scroll session

                        val lastSessionFromDao = scrollEventDao.getLastSession()

                        Log.d("DatabaseCheck", lastSessionFromDao.toString())
                        val lastSession = lastSession(lastSessionFromDao, keyboardEventDao.getTypesAfterTimestamp(lastSessionFromDao[0].timestamp) )
                        scrollSessionDao.insert(lastSession)
                        Log.d("DatabaseCheck", "A new session has been created $lastSession")

                        // on supprime les scrolls de la session résumée
                        scrollEventDao.deleteScrolls()
                        clickEventDao.deleteClicks()
                        keyboardEventDao.deleteTypes()
                        Log.d("DatabaseCheck", "the last scrolls left : ${scrollEventDao.getAllScrollEvents()}")




                    }
                    else {

                        scrollEventDao.insert(scrollEvent)
                        Log.d("DatabaseCheck", "New event added : ${scrollEventDao.getLastScrollEvent()}")
                        // on  vérifie si dans la dernière session il y a 30 scroll
                        //si oui -> on analyse
                        //si non -> on ne fait r
                        val scrollsSinceLastSessionStart = scrollEventDao.getScrollsSinceLastSessionStart()
                        Log.d("DatabaseCheck", scrollsSinceLastSessionStart.size.toString())
                        if (scrollsSinceLastSessionStart.size >= 5) {


                            //ceci est la parti temporaire
                            val curentSession = lastSession(scrollsSinceLastSessionStart, keyboardEventDao.getTypesAfterTimestamp(scrollsSinceLastSessionStart[0].timestamp))
                            Log.d("DatabaseCheck", curentSession.toString())
                            if (curentSession.isDumpScroll) {
                                Log.d("DatabaseCheck", "${scrollEventDao.getAllScrollEvents()} \n ${keyboardEventDao.getKeyboardEvents()}")
                                Log.d("DatabaseCheck", "BLOCCCKKKKKKKK STOPPPPPP NOWWWWWW !!!!!")



                                showNotification(this@MyAccessibilityService, "Scrolling Alert", "You have been scrolling for too long!")

                                val context = this@MyAccessibilityService // 'this' refers to the current Activity or Service
                                launch(Dispatchers.Main) {
                                    showOverlay(context, summarizeSessionForAI(scrollsSinceLastSessionStart, clickEventDao.getClicksAfterTimestamp(scrollsSinceLastSessionStart[0].timestamp) ,keyboardEventDao.getTypesAfterTimestamp(scrollsSinceLastSessionStart[0].timestamp))) // Pass context to the showOverlay function
                                }
                                scrollEventDao.deleteScrolls()
                                keyboardEventDao.deleteTypes()
                                clickEventDao.deleteClicks()

                            }





                            //ici on va coder la partie qui nous permettra d'implémenter l'ia//////////////////////////////////////////

                            val summarizeSessionGoodForAI = summarizeSessionForAI(scrollsSinceLastSessionStart, clickEventDao.getClicksAfterTimestamp(scrollsSinceLastSessionStart[0].timestamp), keyboardEventDao.getTypesAfterTimestamp(scrollsSinceLastSessionStart[0].timestamp))
                            Log.d ("DatabaseCheck", "$summarizeSessionGoodForAI")
                            //ensuite il s'agira de trier selon package





                        }
                    }
                }
                //si c'est le premier scroll all time
                else{
                    scrollEvent.isAScrollSessionStart = true
                    scrollEventDao.insert(scrollEvent)
                    Log.d("DatabaseCheck", "New event added : ${scrollEventDao.getLastScrollEvent()}")
                }

                //résumer la dernière session





                val editor = sharedPreferences.edit()
                editor.putLong("last_timestamp", scrollEvent.timestamp)
                editor.apply()
                Log.d("DatabaseCheck", "New shared pref : ${scrollEvent.timestamp}, 1")
            }
            else if(scrollEvent.timestamp - value <= 300){
                //Log.d("DatabaseCheck", "time diff since last : ${scrollEvent.timestamp - value}, 2")
                val editor = sharedPreferences.edit()
                editor.putLong("last_timestamp", scrollEvent.timestamp)
                editor.apply()
                //Log.d("DatabaseCheck", "New shared pref : ${scrollEvent.timestamp}, 2")
            }
            else{
                Log.d("DatabaseCheck", "wtf")
            }
        }

        /////maintenant on va analyser les données jusque là et vois si c'est oklm ou si on bloque

    }

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null


    fun showOverlay(context: MyAccessibilityService<Button>, sessionForAI: SessionForAI) {
        // Obtention du WindowManager
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Création de la vue d'overlay à partir du layout XML
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val overlayView = inflater.inflate(R.layout.overlay_layout, null)

        // Paramètres de l'overlay
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        layoutParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL

        // Bouton Oui
        val buttonYes = overlayView.findViewById<Button>(R.id.button_yes)
        if (buttonYes != null) {
            buttonYes.setOnClickListener {
                // Logique pour capturer la réponse Oui
                Toast.makeText(this, "Yes selected", Toast.LENGTH_SHORT).show()
                sessionForAI.isDumpScroll = true
                Log.d("Check", "here is the session for AI: ${sessionForAI}")
                // Insérer la logique pour l'IA ou l'enregistrement des données ici
                // Exemple : enregistrer l'info pour l'IA
                saveSessionToRealtimeDatabase(sessionForAI)
            }
        }

        // Bouton Non
        val buttonNo = overlayView.findViewById<Button>(R.id.button_no)
        if (buttonNo != null) {
            buttonNo.setOnClickListener {
                // Logique pour capturer la réponse Non
                Toast.makeText(this, "No selected", Toast.LENGTH_SHORT).show()
                // Logique pour l'IA ou enregistrement des données
                sessionForAI.isDumpScroll = false
                Log.d("Check", "here is the session for AI: ${sessionForAI}")
                saveSessionToRealtimeDatabase(sessionForAI)
            }
        }

        // Bouton Continuer à scroller
        val buttonContinue = overlayView.findViewById<Button>(R.id.button_continue)
        if (buttonContinue != null) {
            buttonContinue.setOnClickListener {
                // Logique pour continuer à scroller
                Toast.makeText(this, "Continue scrolling", Toast.LENGTH_SHORT).show()
                windowManager.removeView(overlayView)  // Masquer l'overlay
            }
        }

        // Bouton Quitter
        val buttonQuit = overlayView.findViewById<Button>(R.id.button_quit)
        if (buttonQuit != null) {
            buttonQuit.setOnClickListener {
                // Logique pour quitter et retourner à l'écran d'accueil
                Toast.makeText(this, "Quitting app", Toast.LENGTH_SHORT).show()
                windowManager.removeView(overlayView)  // Masquer l'overlay

                // Lancer l'intention pour aller à l'écran d'accueil
                val homeIntent = Intent(Intent.ACTION_MAIN)
                homeIntent.addCategory(Intent.CATEGORY_HOME)
                homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(homeIntent)
            }
        }

        // Affichage de l'overlay
        windowManager.addView(overlayView, layoutParams)
    }

    private fun finish() {
        TODO("Not yet implemented")
    }


    private fun checkOverlayPermission(context: Context) {
        if (!Settings.canDrawOverlays(context)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }



    // ici on bosse pour l'ia
    private fun summarizeSessionForAI(scrollSession: List<ScrollEvent>, clicks: List<ClickEvent>, types: List<KeyboardEvent>): SessionForAI {
        val sessionDuration = scrollSession[scrollSession.size - 1].timestamp - scrollSession[0].timestamp
        Log.d("DatabaseCheck", "$sessionDuration")
        val averageScrollFrequencyInScrollsPerMinute: Float
        if (sessionDuration > 0) {
            averageScrollFrequencyInScrollsPerMinute = scrollSession.size / (sessionDuration / 60000.0f)
            Log.d("DatabaseCheck", "Average scroll frequency: $averageScrollFrequencyInScrollsPerMinute")
        } else {
            Log.d("DatabaseCheck", "Session duration is zero, cannot calculate frequency.")
            averageScrollFrequencyInScrollsPerMinute = 1000f
        }




        val irregularityScore: Float

        if (scrollSession.size >= 3) {
            var irregularCount = 0

            for (i in 0 until scrollSession.size - 2) {
                val timeBetweenScrolls = scrollSession[i + 1].timestamp - scrollSession[i].timestamp
                val timeToNextScroll = scrollSession[i + 2].timestamp - scrollSession[i + 1].timestamp

                if (timeToNextScroll > 4 * timeBetweenScrolls) {
                    irregularCount++
                }
            }
            irregularityScore = irregularCount.toFloat() / (scrollSession.size - 2).toFloat() // Normalize
        } else {
            irregularityScore = 0f
        }
        val averageTimeBetweenClicks: Float
        if (clicks.isNotEmpty()) {
            averageTimeBetweenClicks =
                clicks.size.toFloat() / ((scrollSession[scrollSession.size - 1].timestamp - scrollSession[0].timestamp).toFloat() / 60000f)
        }else {
            averageTimeBetweenClicks = 1000f
        }
        var sessionDominantApp: String
        val appScrollCount = mutableMapOf<String, Int>()
        for (scroll in scrollSession) {
            appScrollCount[scroll.packageName] = appScrollCount.getOrDefault(scroll.packageName, 0) + 1
        }
        val dominantApp = appScrollCount.maxByOrNull { it.value }
        if (dominantApp != null && (dominantApp.value.toFloat() / scrollSession.size) > 0.8) {
            // Store the dominant app as the representative for this session
            sessionDominantApp = dominantApp.key
        } else {
            // Handle the case where no app dominates (e.g., consider it as no significant scrolling)
            sessionDominantApp = "None" // or another relevant indicator
        }
        val clickScrollRatio = clicks.size.toFloat() / scrollSession.size.toFloat()
        val typeScrollRatio = types.size.toFloat() / scrollSession.size.toFloat()
        return SessionForAI(averageScrollFrequecy = averageScrollFrequencyInScrollsPerMinute, clickScrollRatio = clickScrollRatio, dominantApp = sessionDominantApp, timeBetweenClicks = averageTimeBetweenClicks, typeScrollRatio = typeScrollRatio, variationScore = irregularityScore )
    }

    fun saveSessionToRealtimeDatabase(session: SessionForAI) {
        // Connecter à la base de données Realtime Database avec l'URL spécifique
        val database = Firebase.database("https://no-more-scrolling-default-rtdb.europe-west1.firebasedatabase.app/")

        // Référence à l'emplacement où stocker les sessions
        val sessionsRef = database.getReference("scrollSessions")

        // Ajouter la session à la base de données avec une clé unique
        sessionsRef.push().setValue(session)
    }


    override fun onDestroy() {
        super.onDestroy()
        windowManager?.removeView(overlayView)
    }

    override fun onInterrupt() {
        // Code à exécuter lorsque le service est interrompu
    }
}
