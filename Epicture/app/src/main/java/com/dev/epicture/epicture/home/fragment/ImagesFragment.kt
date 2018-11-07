package com.dev.epicture.epicture.home.fragment

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dev.epicture.epicture.MyApplication
import com.dev.epicture.epicture.R
import com.dev.epicture.epicture.home.Adapter.ImagesFragmentItemAdapter
import com.dev.epicture.epicture.imgur.service.ImgurService
import com.dev.epicture.epicture.imgur.service.models.ImageModel


class ImagesFragment : GalleryFragment() {

    private lateinit var mView: View
    private lateinit var adapter: ImagesFragmentItemAdapter

    private var images: ArrayList<ImageModel> = ArrayList()
    private var loading: Boolean = false
    private var page: Int = 0
    private var all = true



    // Select all Activation
    private fun activateSelectAll(recyclerView: RecyclerView) {
        // Reload activation
        menuManager.selectAll.isVisible = true
        menuManager.selectAll.setOnMenuItemClickListener {
            val adapter = recyclerView.adapter as ImagesFragmentItemAdapter
            adapter.selecting = all
            for (elem in images)
                elem.selected = all
            all = !all
            adapter.notifyDataSetChanged()
            return@setOnMenuItemClickListener true
        }
    }

    // Reload Activation
    private fun activateReload(recyclerView: RecyclerView) {
        // Reload activation
        menuManager.refresh.isVisible = true
        menuManager.refresh.setOnMenuItemClickListener {
            loadActivePages(recyclerView)
            return@setOnMenuItemClickListener true
        }
    }

    // Delete activation
    private fun activateDelete(recyclerView: RecyclerView) {
        menuManager.delete.setOnMenuItemClickListener {
            val adapter = recyclerView.adapter as ImagesFragmentItemAdapter
            val removed = adapter.applySelection()
            for (elem in removed)
                images.remove(elem)
            deleteImages(removed)
            recyclerView.adapter?.notifyDataSetChanged()
            return@setOnMenuItemClickListener true
        }
    }

    // Cancel activation
    private fun activateCancelSelection(recyclerView: RecyclerView) {
        menuManager.cancel.setOnMenuItemClickListener {
            val adapter = recyclerView.adapter as ImagesFragmentItemAdapter
            val selected = adapter.applySelection()
            selected.forEach { image ->
                image.selected = false
            }
            recyclerView.adapter?.notifyDataSetChanged()
            return@setOnMenuItemClickListener true
        }
    }

    // Infinite scroll activation
    private fun activateInfiniteScroll(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1) && !loading) {
                    val size = images.size
                    loading = true
                    loadImagesPage(page + 1) {
                        if (images.size > size) {
                            page += 1
                        }
                        loading = false
                        activity!!.runOnUiThread {
                            recyclerView.adapter?.notifyDataSetChanged()
                        }
                    }
                }
            }
        })
    }

    private fun createRecyclerView() {
        adapter = ImagesFragmentItemAdapter(images, context!!, menuManager)
        val recyclerView = mView.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = adapter

        loadActivePages(recyclerView)
        activateReload(recyclerView)
        activateDelete(recyclerView)
        activateCancelSelection(recyclerView)
        activateInfiniteScroll(recyclerView)
        activateSelectAll(recyclerView)

    }

    private fun loadActivePages(recyclerView: RecyclerView) {
        if (loading)
            return
        images.clear()
        loading = true
        for (i in 0..page)
            loadImagesPage(i) {
                if (i == page) {
                    activity!!.runOnUiThread {
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                    loading = false
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_gallery_images, container, false)
        createRecyclerView()
        return mView
    }

    // add a page in images array and update recycler view
    private fun loadImagesPage(page: Int, callback: () -> Unit = {}) {
        ImgurService.getImages({ resp ->
            try {
                for (image in resp.data)
                    images.add(image)
                images.distinctBy { it.id }
            } catch (e : Exception) {
                MyApplication.printMessage("Failed to load images page $page")
            }
            callback()
        }, {
            MyApplication.printMessage("Failed to load images page $page")
            callback()
        }, page.toString())
    }

    // delete select item from array
    private fun deleteImages(images: ArrayList<ImageModel>) {
        for (image in images) {
            ImgurService.deleteImage({
            }, {
                MyApplication.printMessage("Failed to delete image ${image.id}")
            }, image.id!!)
        }

    }

    override fun getSearchListener(): SearchView.OnQueryTextListener {
        return object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                adapter.filter.filter(query)
                return false
            }

        }
    }

}

