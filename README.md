# Owl Read Mobile App - Built using Kotlin and Jetpack Compose

## Overview

This is a audiobook listener app built using Kotlin, Jetpack Compose, MVVM Architecture Retrofit, Paging, Glide, Shared Preferences, and more.

The app fetches audiobook from an API and uses pagination to list them. By clicking on any audiobook, the app will fetch their chapters information including the book's cover image, duration. By clicking on any chapter user will see a player screen where user can listen to the selected chapter. The app also supports background playback where the user can use the notification for controlling the playback. All of these actions are performed using network requests.

## Features
* Pagination for audiobook listing.
* View every chapter by clicking on the audiobook.
* Creates cache to save time for chapter listing.
* Supports background play with Audio Focus.
* Notification controls for playback and other functionalities.

### The player screen consist the following features
* The user can play pause the audiobook
* Next chapter will automatically start playing as soon as the current chapter ends.
* The user can control the playback speed, slider position to move the playback.
* The user can move to next or previous chapter.
* The user can seek the playback.

## Technologies used

* Kotlin
* Jetpack Compose
* Compose Navigation
* MVVM Architecture
* Retrofit
* Paging
* Coil
* Shared Preferences
* Audio Focus

## Installation
Clone the repository and import the project into Android Studio. The required dependencies are specified in the build.gradle files.
