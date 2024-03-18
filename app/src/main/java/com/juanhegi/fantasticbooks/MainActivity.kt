package com.juanhegi.fantasticbooks

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.juanhegi.fantasticbooks.databinding.ActivityMainBinding
import com.juanhegi.fantasticbooks.ui.user.UserViewModel
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity(){

    private var menu: Menu? = null
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var user: UserViewModel
    private lateinit var profileButton: Button
    private lateinit var fullname: TextView
    private lateinit var email: TextView
    private lateinit var image: ImageView
    private lateinit var drawerLayout: DrawerLayout

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        user = ViewModelProvider(this)[UserViewModel::class.java]
        user.user = null
        //User.User("1", "Juan","pepe","pruebas@gmail.com",null,null, LocalDate.now(), "admin",false)
        Thread.sleep(1000)
        setTheme(R.style.Theme_FantasticBooks_NoActionBar)
        //android:theme="@style/Theme.FantasticBooks.NoActionBar">
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_genre, R.id.nav_login
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        profileButton = navView.getHeaderView(0).findViewById<Button>(R.id.btnProfile)
        fullname = navView.getHeaderView(0).findViewById<TextView>(R.id.fullName)
        email = navView.getHeaderView(0).findViewById<TextView>(R.id.email)
        image = navView.getHeaderView(0).findViewById<ImageView>(R.id.imageHeader)
        logOut()
        profileButton.setOnClickListener {
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            navController.navigate(R.id.nav_profile)
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        val menu: Menu = navView.menu
        val logoutItem: MenuItem = menu.findItem(R.id.nav_logout)
        logoutItem.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    logOut()
                    true // Devolver true indica que el evento ha sido manejado
                }

                else -> false // Devolver false si el evento no ha sido manejado
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        this.menu = menu
        menu.setGroupVisible(0, false)
        return true
    }

    fun getMenu(): Menu? {
        return this.menu
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()

    }

    fun login() {
        profileButton.isVisible = true
        email.text = user.user?.email
        update()
        binding.navView.menu.findItem(R.id.nav_login)?.isVisible = false
        binding.navView.menu.findItem(R.id.nav_logout)?.isVisible = true
    }

    fun logOut() {
        profileButton.isVisible = false
        fullname.text = "Usuario sin registrar"
        email.text = "FantasticBooks@ejemplo.com"
        image.setImageResource(R.drawable.icon_account)
        binding.navView.menu.findItem(R.id.nav_login)?.isVisible = true
        binding.navView.menu.findItem(R.id.nav_logout)?.isVisible = false
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        navController.navigate(R.id.nav_home)
        drawerLayout.closeDrawer(GravityCompat.START)

    }

    fun update() {
        fullname.text = user.user?.name + " " + user.user?.lastname
        if (user.user?.imageUrl == null) {
            image.setImageResource(R.drawable.icon_account)
        } else {
            Picasso.get().load(user.user?.imageUrl).into(image)
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_edit ->{
                Toast.makeText(this,"editar", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.action_lend ->{
                Toast.makeText(this,"prestar", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_return ->{
                Toast.makeText(this,"devolver", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_delete ->{
                Toast.makeText(this,"eliminar", Toast.LENGTH_SHORT).show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
        return true
    }
}