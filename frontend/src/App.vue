<template>
  <div id="app" class="bg-gray-50 min-h-screen">
    <!-- Navigation Bar -->
    <nav class="bg-white shadow">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <div class="flex-shrink-0 flex items-center">
              <h1 class="text-2xl font-bold text-blue-600">REme</h1>
            </div>
            <div class="hidden md:block">
              <div class="ml-10 flex items-baseline space-x-4">
                <router-link 
                  to="/" 
                  class="px-3 py-2 rounded-md text-sm font-medium hover:text-blue-600 transition"
                  :class="$route.path === '/' ? 'bg-blue-50 text-blue-600' : 'text-gray-700'"
                >
                  Dashboard
                </router-link>
                <router-link 
                  to="/groups" 
                  class="px-3 py-2 rounded-md text-sm font-medium hover:text-blue-600 transition"
                  :class="$route.path === '/groups' ? 'bg-blue-50 text-blue-600' : 'text-gray-700'"
                >
                  Gruppen
                </router-link>
                <router-link 
                  to="/activities" 
                  class="px-3 py-2 rounded-md text-sm font-medium hover:text-blue-600 transition"
                  :class="$route.path === '/activities' ? 'bg-blue-50 text-blue-600' : 'text-gray-700'"
                >
                  Aktivitäten
                </router-link>
              </div>
            </div>
          </div>
          <div class="flex items-center">
            <div class="ml-4 flex items-center md:ml-6">
              <button class="p-1 rounded-full text-gray-400 hover:text-gray-600 focus:outline-none">
                <span class="sr-only">View notifications</span>
                <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>
                </svg>
              </button>
              <div class="ml-3 relative">
                <button class="max-w-xs bg-white rounded-full flex items-center text-sm focus:outline-none">
                  <svg class="h-8 w-8 rounded-full bg-gray-300 text-white p-1" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
}

html, body, #app {
  height: 100%;
  width: 100%;
}
</style>
