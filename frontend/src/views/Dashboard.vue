<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="border-b border-gray-200 pb-6">
      <h1 class="text-3xl font-bold text-gray-900">Dashboard</h1>
      <p class="text-gray-600 mt-2">Willkommen! Hier ist dein persönlicher Überblick.</p>
    </div>

    <!-- Stats Cards Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="bg-white rounded-lg shadow p-6 border-l-4 border-blue-500">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-gray-600 text-sm">Meine Gruppen</p>
            <p class="text-2xl font-bold text-gray-900">{{ myGroups.length }}</p>
          </div>
          <svg class="w-12 h-12 text-blue-100" fill="currentColor" viewBox="0 0 24 24">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z"/>
          </svg>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow p-6 border-l-4 border-yellow-500">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-gray-600 text-sm">Einladungen</p>
            <p class="text-2xl font-bold text-gray-900">{{ pendingInvitationsCount }}</p>
          </div>
          <svg class="w-12 h-12 text-yellow-100" fill="currentColor" viewBox="0 0 24 24">
            <path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h14l4 4V6c0-1.1-.9-2-2-2zm0 12H6v-2h14v2zm0-3H6V9h14v4zm0-4H6V5h14v4z"/>
          </svg>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow p-6 border-l-4 border-green-500">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-gray-600 text-sm">Meine Aktivitäten</p>
            <p class="text-2xl font-bold text-gray-900">{{ myActivities.length }}</p>
          </div>
          <svg class="w-12 h-12 text-green-100" fill="currentColor" viewBox="0 0 24 24">
            <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14zm-5.04-6.71l-2.75 3.54-2.04-2.71c-.42-.57-1.27-.57-1.68 0-.41.57-.41 1.45 0 2.02l2.87 3.83c.82 1.09 2.47 1.09 3.29 0l4.42-5.88c.41-.57.41-1.45 0-2.02-.42-.57-1.27-.57-1.68 0l-3.13 4.22z"/>
          </svg>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow p-6 border-l-4 border-red-500">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-gray-600 text-sm">Ausstehend</p>
            <p class="text-2xl font-bold text-gray-900">{{ pendingActivitiesCount }}</p>
          </div>
          <svg class="w-12 h-12 text-red-100" fill="currentColor" viewBox="0 0 24 24">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
          </svg>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- My Groups Section -->
      <div class="bg-white rounded-lg shadow overflow-hidden">
        <div class="bg-gradient-to-r from-blue-600 to-blue-700 px-6 py-4">
          <h2 class="text-xl font-bold text-white">Meine Gruppen</h2>
        </div>
        <div class="p-6">
          <MyGroupsCard 
            :groups="myGroups" 
            :is-loading="isLoadingGroups"
            :error="errorGroups"
          />
        </div>
      </div>

      <!-- Group Invitations Section -->
      <div class="bg-white rounded-lg shadow overflow-hidden">
        <div class="bg-gradient-to-r from-yellow-600 to-yellow-700 px-6 py-4">
          <h2 class="text-xl font-bold text-white">Gruppeneinladungen</h2>
          <p class="text-yellow-100 text-sm mt-1" v-if="hasInvitations">{{ pendingInvitationsCount }} ausstehend</p>
        </div>
        <div class="p-6">
          <GroupInvitationsCard 
            :invitations="groupInvitations"
            :is-loading="isLoadingInvitations"
            :error="errorInvitations"
            @accept="acceptInvitation"
            @decline="declineInvitation"
          />
        </div>
      </div>

      <!-- My Activities Section -->
      <div class="bg-white rounded-lg shadow overflow-hidden">
        <div class="bg-gradient-to-r from-green-600 to-green-700 px-6 py-4">
          <h2 class="text-xl font-bold text-white">Meine Aktivitäten</h2>
        </div>
        <div class="p-6">
          <MyActivitiesCard 
            :activities="upcomingActivities" 
            :is-loading="isLoadingActivities"
            :error="errorActivities"
          />
        </div>
      </div>

      <!-- Pending Activities Section -->
      <div class="bg-white rounded-lg shadow overflow-hidden">
        <div class="bg-gradient-to-r from-red-600 to-red-700 px-6 py-4">
          <h2 class="text-xl font-bold text-white">Ausstehende Aktivitäten</h2>
          <p class="text-red-100 text-sm mt-1" v-if="hasPendingActivities">{{ pendingActivitiesCount }} Antworten erforderlich</p>
        </div>
        <div class="p-6">
          <PendingActivitiesCard 
            :activities="pendingActivities"
            :is-loading="isLoadingPending"
            :error="errorPending"
            @accept="acceptActivity"
            @decline="declineActivity"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useDashboardGroups } from '../composables/useDashboardGroups'
import { useDashboardActivities } from '../composables/useDashboardActivities'
import MyGroupsCard from '../components/Dashboard/MyGroupsCard.vue'
import GroupInvitationsCard from '../components/Dashboard/GroupInvitationsCard.vue'
import MyActivitiesCard from '../components/Dashboard/MyActivitiesCard.vue'
import PendingActivitiesCard from '../components/Dashboard/PendingActivitiesCard.vue'

const {
  myGroups,
  groupInvitations,
  isLoadingGroups,
  isLoadingInvitations,
  errorGroups,
  errorInvitations,
  acceptInvitation,
  declineInvitation,
  hasInvitations,
  pendingInvitationsCount
} = useDashboardGroups()

const {
  myActivities,
  pendingActivities,
  isLoadingActivities,
  isLoadingPending,
  errorActivities,
  errorPending,
  acceptActivity,
  declineActivity,
  hasPendingActivities,
  pendingActivitiesCount,
  upcomingActivities
} = useDashboardActivities()
</script>

<style scoped>
/* Custom scrollbar */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>
