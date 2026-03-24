<template>
  <div class="space-y-6">
    <div class="border-b border-gray-200 pb-6">
      <h1 class="text-3xl font-bold text-gray-900">Dashboard</h1>
      <p class="text-gray-600 mt-2">Willkommen! Hier ist dein persönlicher Überblick.</p>
    </div>

    <div class="space-y-6">
      <section class="bg-white rounded-lg shadow p-6 space-y-5">
        <h2 class="text-2xl font-semibold text-gray-900">Gruppen</h2>

        <div>
          <h3 class="text-lg font-semibold text-gray-900 mb-3">Aktive Gruppen</h3>
          <div class="overflow-x-auto">
            <table class="w-full table-auto">
              <thead class="bg-gray-50/70">
                <tr>
                  <th class="px-3 py-2 text-left text-sm font-semibold text-gray-700">Name</th>
                  <th class="px-3 py-2 text-left text-sm font-semibold text-gray-700">Beschreibung</th>
                  <th class="px-3 py-2 text-left text-sm font-semibold text-gray-700">Gruppenleiter</th>
                  <th class="px-3 py-2 text-left text-sm font-semibold text-gray-700">Anzahl Mitglieder</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="isLoadingGroups">
                  <td colspan="4" class="px-3 py-3 text-sm text-gray-500">Lade Gruppen...</td>
                </tr>
                <tr v-else-if="errorGroups">
                  <td colspan="4" class="px-3 py-3 text-sm text-red-600">{{ errorGroups }}</td>
                </tr>
                <tr v-else-if="myGroups.length === 0">
                  <td colspan="4" class="px-3 py-3 text-sm text-gray-500">Keine aktiven Gruppen vorhanden.</td>
                </tr>
                <tr
                  v-for="(group, index) in myGroups"
                  v-else
                  :key="String(readField(group, ['id', 'groupId', 'name']) ?? index)"
                >
                  <td class="px-3 py-2 text-sm text-gray-900">{{ displayGroupName(group) }}</td>
                  <td class="px-3 py-2 text-sm text-gray-700">{{ displayGroupDescription(group) }}</td>
                  <td class="px-3 py-2 text-sm text-gray-700">{{ displayGroupLeader(group) }}</td>
                  <td class="px-3 py-2 text-sm text-gray-700">{{ displayMemberCount(group) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div>
          <h3 class="text-lg font-semibold text-gray-900 mb-3">Gruppeneinladungen</h3>
          <div class="overflow-x-auto">
            <table class="w-full table-auto">
              <thead class="bg-gray-50/70">
                <tr>
                  <th class="px-3 py-2 text-left text-sm font-semibold text-gray-700">Name</th>
                  <th class="px-3 py-2 text-left text-sm font-semibold text-gray-700">Eingeladen durch</th>
                  <th class="px-3 py-2 text-left text-sm font-semibold text-gray-700">Aktionen</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="isLoadingInvitations">
                  <td colspan="3" class="px-3 py-3 text-sm text-gray-500">Lade Gruppeneinladungen...</td>
                </tr>
                <tr v-else-if="errorInvitations">
                  <td colspan="3" class="px-3 py-3 text-sm text-red-600">{{ errorInvitations }}</td>
                </tr>
                <tr v-else-if="groupInvitations.length === 0">
                  <td colspan="3" class="px-3 py-3 text-sm text-gray-500">Keine Gruppeneinladungen vorhanden.</td>
                </tr>
                <tr
                  v-for="(invitation, index) in groupInvitations"
                  v-else
                  :key="String(readField(invitation, ['id', 'invitationId', 'groupInvitationId']) ?? index)"
                >
                  <td class="px-3 py-2 text-sm text-gray-900">{{ displayInvitationName(invitation) }}</td>
                  <td class="px-3 py-2 text-sm text-gray-700">{{ displayInvitationInviter(invitation) }}</td>
                  <td class="px-3 py-2 text-sm text-gray-700">
                    <div class="flex items-center gap-2">
                      <button
                        type="button"
                        class="px-3 py-1.5 text-xs font-medium rounded bg-green-600 text-white hover:bg-green-700"
                        @click="handleAcceptInvitation(invitation)"
                      >
                        Akzeptieren
                      </button>
                      <button
                        type="button"
                        class="px-3 py-1.5 text-xs font-medium rounded bg-red-600 text-white hover:bg-red-700"
                        @click="handleDeclineInvitation(invitation)"
                      >
                        Ablehnen
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <section class="bg-white rounded-lg shadow p-6 space-y-5">
        <h2 class="text-2xl font-semibold text-gray-900">Aktivitäten</h2>

        <div>
          <h3 class="text-lg font-semibold text-gray-900 mb-3">Anstehende Aktivitäten</h3>
          <MyActivitiesCard
            :activities="upcomingActivities"
            :is-loading="isLoadingActivities"
            :error="errorActivities"
          />
        </div>

        <div>
          <h3 class="text-lg font-semibold text-gray-900 mb-3">Einladungen zu Aktivitäten</h3>
          <PendingActivitiesCard
            :activities="pendingActivities"
            :is-loading="isLoadingPending"
            :error="errorPending"
            @accept="acceptActivity"
            @decline="declineActivity"
          />
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useDashboardGroups } from '../composables/useDashboardGroups'
import { useDashboardActivities } from '../composables/useDashboardActivities'
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
  declineInvitation
} = useDashboardGroups()

const {
  pendingActivities,
  isLoadingActivities,
  isLoadingPending,
  errorActivities,
  errorPending,
  acceptActivity,
  declineActivity,
  upcomingActivities
} = useDashboardActivities()

const asRecord = (value: unknown): Record<string, unknown> =>
  value !== null && typeof value === 'object' ? (value as Record<string, unknown>) : {}

const readField = (group: unknown, keys: string[]): unknown => {
  const item = asRecord(group)
  for (const key of keys) {
    const value = item[key]
    if (value !== undefined && value !== null && value !== '') return value
  }
  return undefined
}

const displayGroupName = (group: unknown): string =>
  String(readField(group, ['name', 'groupName', 'title']) ?? '-')

const displayGroupDescription = (group: unknown): string =>
  String(readField(group, ['description', 'groupDescription', 'desc']) ?? '-')

const displayGroupLeader = (group: unknown): string =>
  String(readField(group, ['groupLeader', 'leaderName', 'ownerName', 'createdByName']) ?? '-')

const displayMemberCount = (group: unknown): string => {
  const directCount = readField(group, ['memberCount', 'membersCount', 'numberOfMembers'])
  if (directCount !== undefined) return String(directCount)

  const members = readField(group, ['members', 'memberList'])
  if (Array.isArray(members)) return String(members.length)

  return '-'
}

const displayInvitationName = (invitation: unknown): string =>
  String(readField(invitation, ['groupName', 'name', 'title']) ?? '-')

const displayInvitationInviter = (invitation: unknown): string =>
  String(readField(invitation, ['invitedByName', 'inviterName', 'createdByName', 'groupLeader']) ?? '-')

const getInvitationId = (invitation: unknown): number | null => {
  const candidate = readField(invitation, ['id', 'invitationId', 'groupInvitationId'])
  const parsed = Number(candidate)
  return Number.isFinite(parsed) ? parsed : null
}

const handleAcceptInvitation = async (invitation: unknown) => {
  const invitationId = getInvitationId(invitation)
  if (invitationId === null) return
  await acceptInvitation(invitationId)
}

const handleDeclineInvitation = async (invitation: unknown) => {
  const invitationId = getInvitationId(invitation)
  if (invitationId === null) return
  await declineInvitation(invitationId)
}
</script>

<style scoped>
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
