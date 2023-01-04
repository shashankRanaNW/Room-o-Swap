package com.bottlerunner.room_o_swap


import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.bottlerunner.room_o_swap.data.UserApna
import com.bottlerunner.room_o_swap.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects

class HomeFragment : BaseFragment(R.layout.fragment_home) {

    private lateinit var binding:FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_home,container,false)


        binding.swipeRefresh.setOnRefreshListener {
            updateRVs()
        }

        setHasOptionsMenu(true)

        binding.btnAddRequest.shrink()

        binding.scrollView.setOnScrollChangeListener { view, i, i2, i3, i4 ->
            binding.btnAddRequest.extend()
        }
        binding.btnAddRequest.setOnClickListener{
            Navigation.findNavController(view!!).navigate(R.id.action_homeFragment_to_addRequestFragment)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(FirebaseAuth.getInstance().currentUser == null){
            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_signInOrSignUp)
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        updateRVs()
        super.onStart()
    }

    fun updateRVs(){
//        showProgressDialog("Loading requests please wait")
        FirebaseFirestore.getInstance().collection("users")         //gets the latest data and puts it in rv
            .get().addOnCompleteListener { it ->
                if (it.isSuccessful) {                                                    //TODO: oh git!, unable to deserialise yet again
                    Database.userList.clear()
                    Database.requestList.clear()
                    Database.userList = it.result.toObjects<UserApna>().toMutableList()
                    for (i in Database.userList) {
                        if (i.requestList.size != 0) {
                            for (j in i.requestList)
                                Database.requestList.add(j)
                        }
                    }
                    binding.rvMatchesAvailable.adapter =MatchAdapter(currContext, Database.requestList)
                    binding.rvMatchesAvailable.layoutManager = LinearLayoutManager(currContext)

                    FirebaseAuth.getInstance().currentUser?. let{
                            it1->
                        var text1=""
                        val currUserId = it1.uid
                        val user = Database.findUserById(currUserId)
                        if (user != null) {

                            var i = 0
                            while( i < user.requestList.size -1){
                                text1 = text1 + user.requestList[i].toHostel + "\n \n"
                                i++
                            }
                            text1 += user.requestList[i].toHostel
                        }
                        binding.tvToCardHome.text = text1
                        if (user != null) {
                            binding.tvFromCardHome.text = user.hostel
                        }
                    }

                    var matchList: MutableList<Request> = mutableListOf()
                    FirebaseAuth.getInstance().currentUser?.let {
                        var currUser = Database.findUserById(it.uid)
                        if (currUser == null) {
                            Toast.makeText(
                                currContext,
                                "Current user not found in database",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(currContext,currUser.name + ", welcome"
                                ,Toast.LENGTH_SHORT)
                            matchList = Database.makeMatchList(currUser)
                        }
                    }                         //adding stuff in your matchesRV
                    binding.rvYourMatches.adapter = MatchAdapter(currContext, matchList)
                    binding.rvYourMatches.layoutManager = LinearLayoutManager(currContext)

                    FirebaseAuth.getInstance().currentUser?.let { it1 ->
                        val user = Database.findUserById(it1.uid)
                        if (user != null) {
                            binding.tvWelcome.text = "Hello ${user.name}, welcome to Room-o-Swap"
                        }
                    }


                } else {
                    Toast.makeText(currContext, it.exception.toString(), Toast.LENGTH_SHORT)
                        .show()
                }

                binding.swipeRefresh.isRefreshing = false
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item!!,
            view!!.findNavController())
                || super.onOptionsItemSelected(item)
    }

}