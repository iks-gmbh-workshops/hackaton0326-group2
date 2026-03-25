<template>
  <div>
    <h1 class="app-page-title mb-6">Gruppen</h1>
    <p v-if="successMessage" class="app-alert-success mb-4">
      {{ successMessage }}
    </p>
    
    <div class="grid grid-cols-1 gap-6">
      <div class="bg-white rounded-lg shadow p-6 space-y-5">
        <div class="flex flex-wrap items-center justify-between gap-3">
          <h2 class="app-card-title">Meine Gruppen</h2>
          <div class="flex flex-wrap items-center gap-2">
            <button
              type="button"
              class="app-btn-primary"
              @click="openCreateForm"
            >
              + Neue Gruppe erstellen
            </button>
            <button
              type="button"
              class="app-btn-secondary"
              :disabled="isJoiningByToken"
              :class="{ 'cursor-not-allowed opacity-70': isJoiningByToken }"
              @click="openJoinByTokenForm"
            >
              Mit Einladungstoken beitreten
            </button>
          </div>
        </div>

        <div class="overflow-x-auto">
          <table class="app-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Beschreibung</th>
                <th>Erstellt durch</th>
                <th>Anzahl der Mitglieder</th>
                <th class="app-table-col-right">Aktion</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="isLoadingGroups">
                <td colspan="5" class="app-table-cell-muted">Lade Gruppen...</td>
              </tr>
              <tr v-else-if="groupsError">
                <td colspan="5" class="app-table-cell-error">{{ groupsError }}</td>
              </tr>
              <tr v-else-if="groups.length === 0">
                <td colspan="5" class="app-table-cell-muted">Keine Gruppen vorhanden.</td>
              </tr>
              <tr v-for="group in groups" v-else :key="group.id">
                <td class="app-table-cell-main">{{ group.name }}</td>
                <td>{{ group.description || '-' }}</td>
                <td>{{ group.createdBy || '-' }}</td>
                <td>{{ group.memberCount ?? '-' }}</td>
                <td class="app-table-cell-right">
                  <div class="inline-flex items-center gap-1">
                    <button
                      type="button"
                      class="inline-flex h-8 w-8 items-center justify-center rounded-md text-gray-600 transition hover:bg-gray-100 hover:text-blue-700"
                      aria-label="Gruppe ansehen"
                      :disabled="isSubmitting || isEditSubmitting || isDeletingGroup"
                      @click="openViewForm(group)"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" class="h-4 w-4">
                        <path d="M10 3c4.204 0 7.663 2.687 8.819 6.433a1.5 1.5 0 0 1 0 .934C17.663 14.113 14.204 16.8 10 16.8S2.337 14.113 1.18 10.367a1.5 1.5 0 0 1 0-.934C2.337 5.687 5.796 3 10 3Zm0 2C6.768 5 4.053 6.97 3.06 9.9c.993 2.93 3.708 4.9 6.94 4.9s5.947-1.97 6.94-4.9C15.947 6.97 13.232 5 10 5Zm0 1.7a3.2 3.2 0 1 1 0 6.4 3.2 3.2 0 0 1 0-6.4Z" />
                      </svg>
                    </button>
                    <button
                      v-if="canEditGroup(group.id)"
                      type="button"
                      class="inline-flex h-8 w-8 items-center justify-center rounded-md text-gray-600 transition hover:bg-gray-100 hover:text-blue-700"
                      aria-label="Gruppe bearbeiten"
                      :disabled="isSubmitting || isEditSubmitting || isDeletingGroup"
                      @click="openEditForm(group)"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" class="h-4 w-4">
                        <path d="M13.586 2.586a2 2 0 0 1 2.828 2.828l-8.25 8.25a2 2 0 0 1-.878.513l-3 1a1 1 0 0 1-1.264-1.264l1-3a2 2 0 0 1 .513-.878l8.25-8.25Zm1.414 1.414a.5.5 0 0 0-.707 0L5.99 12.303a.5.5 0 0 0-.128.22l-.62 1.858 1.859-.62a.5.5 0 0 0 .219-.128L15.623 5.33A.5.5 0 0 0 15 4Z" />
                      </svg>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div
      v-if="showCreateForm"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      @click.self="closeCreateForm"
    >
      <form
        class="w-full max-w-2xl max-h-[90vh] overflow-y-auto bg-white rounded-lg shadow-xl p-6 md:p-8 fade-in"
        @submit.prevent="createGroup"
      >
        <div class="flex items-start justify-between mb-6">
          <h2 class="app-card-title">Neue Gruppe anlegen</h2>
          <button
            type="button"
            class="text-gray-500 hover:text-gray-700 text-xl leading-none"
            aria-label="Popup schliessen"
            :disabled="isSubmitting"
            @click="closeCreateForm"
          >
            x
          </button>
        </div>
        <p v-if="errorMessage" class="app-alert-error mb-4">
          {{ errorMessage }}
        </p>

        <div class="space-y-5">
          <div>
            <label for="groupName" class="block text-sm font-medium text-gray-700 mb-1">
              Gruppenname <span class="text-red-600">*</span>
            </label>
            <input
              id="groupName"
              v-model.trim="form.groupName"
              type="text"
              required
              :disabled="isSubmitting"
              placeholder="z. B. Laufgruppe Mittwoch"
              class="w-full rounded-lg border border-gray-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          <div>
            <label for="description" class="block text-sm font-medium text-gray-700 mb-1">
              Beschreibung (optional)
            </label>
            <textarea
              id="description"
              v-model.trim="form.description"
              rows="3"
              :disabled="isSubmitting"
              placeholder="Kurze Beschreibung der Gruppe"
              class="w-full rounded-lg border border-gray-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          <div>
            <label for="memberInput" class="block text-sm font-medium text-gray-700 mb-1">
              Gruppenmitglieder hinzufügen
            </label>
            <div class="flex gap-2">
              <input
                id="memberInput"
                v-model.trim="memberInput"
                type="email"
                :disabled="isSubmitting"
                placeholder="mitglied@beispiel.de"
                class="flex-1 rounded-lg border border-gray-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                @keyup.enter.prevent="addMember"
              />
              <button
                type="button"
                :disabled="isSubmitting"
                class="app-btn-primary"
                :class="{ 'cursor-not-allowed opacity-70': isSubmitting }"
                @click="addMember"
              >
                +
              </button>
            </div>
            <p v-if="memberErrorMessage" class="mt-2 text-sm text-red-600">{{ memberErrorMessage }}</p>

            <ul v-if="form.members.length" class="mt-3 space-y-2">
              <li
                v-for="(member, index) in form.members"
                :key="`${member}-${index}`"
                class="flex items-center justify-between rounded-lg bg-gray-50 px-3 py-2 text-sm text-gray-700"
              >
                <span>{{ member }}</span>
                <button
                  type="button"
                  :disabled="isSubmitting"
                  class="text-red-600 hover:text-red-700"
                  @click="removeMember(index)"
                >
                  Entfernen
                </button>
              </li>
            </ul>
          </div>
        </div>

        <div class="mt-8 pt-5 border-t border-gray-200">
          <button
            type="submit"
            :disabled="isSubmitting"
            class="app-btn-success w-full md:w-auto"
            :class="{ 'cursor-not-allowed opacity-70': isSubmitting }"
          >
            {{ isSubmitting ? 'Gruppe wird angelegt...' : 'Gruppe anlegen' }}
          </button>
        </div>
      </form>
    </div>

    <div
      v-if="showEditForm"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      @click.self="closeEditForm"
    >
      <form
        class="w-full max-w-2xl max-h-[90vh] overflow-y-auto bg-white rounded-lg shadow-xl p-6 md:p-8 fade-in"
        @submit.prevent="updateGroup"
      >
        <div class="flex items-start justify-between mb-6">
          <h2 class="app-card-title">{{ isViewOnlyMode ? 'Gruppe ansehen' : 'Gruppe bearbeiten' }}</h2>
          <button
            type="button"
            class="text-gray-500 hover:text-gray-700 text-xl leading-none"
            aria-label="Popup schliessen"
            :disabled="isEditSubmitting || isDeletingGroup"
            @click="closeEditForm"
          >
            x
          </button>
        </div>
        <p
          v-if="editErrorMessage"
          class="app-alert-error mb-4"
        >
          {{ editErrorMessage }}
        </p>

        <p v-if="isLoadingEditData" class="mb-4 text-sm text-gray-500">Gruppendaten werden geladen...</p>

        <div v-else class="space-y-5">
          <div>
            <label for="editGroupName" class="block text-sm font-medium text-gray-700 mb-1">
              Gruppenname <span class="text-red-600">*</span>
            </label>
            <input
              id="editGroupName"
              v-model.trim="editForm.groupName"
              type="text"
              required
              :disabled="isViewOnlyMode || isEditSubmitting || isDeletingGroup"
              placeholder="z. B. Laufgruppe Mittwoch"
              class="w-full rounded-lg border border-gray-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          <div>
            <label for="editDescription" class="block text-sm font-medium text-gray-700 mb-1">
              Beschreibung (optional)
            </label>
            <textarea
              id="editDescription"
              v-model.trim="editForm.description"
              rows="3"
              :disabled="isViewOnlyMode || isEditSubmitting || isDeletingGroup"
              placeholder="Kurze Beschreibung der Gruppe"
              class="w-full rounded-lg border border-gray-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          <div>
            <label v-if="!isViewOnlyMode" for="editMemberInput" class="block text-sm font-medium text-gray-700 mb-1">
              Gruppenmitglieder hinzufügen
            </label>
            <div v-if="!isViewOnlyMode" class="flex gap-2">
              <input
                id="editMemberInput"
                v-model.trim="editMemberInput"
                type="email"
                :disabled="isViewOnlyMode || isEditSubmitting || isDeletingGroup || isInvitingMember"
                placeholder="mitglied@beispiel.de"
                class="flex-1 rounded-lg border border-gray-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                @keyup.enter.prevent="addEditMember"
              />
              <button
                type="button"
                :disabled="isViewOnlyMode || isEditSubmitting || isDeletingGroup || isInvitingMember"
                class="app-btn-primary"
                :class="{ 'cursor-not-allowed opacity-70': isViewOnlyMode || isEditSubmitting || isDeletingGroup || isInvitingMember }"
                @click="addEditMember"
              >
                {{ isInvitingMember ? '...' : '+' }}
              </button>
            </div>
            <p v-if="editMemberErrorMessage" class="mt-2 text-sm text-red-600">{{ editMemberErrorMessage }}</p>

            <div v-if="editForm.members.length" class="mt-3 overflow-x-auto">
              <table class="app-table">
                <thead>
                  <tr>
                    <th>Mitglied</th>
                    <th>Rolle</th>
                    <th>Status</th>
                    <th>Beitritt</th>
                    <th v-if="!isViewOnlyMode" class="app-table-col-right">Aktion</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="(member, index) in editForm.members"
                    :key="`${member.email || member.label}-${index}`"
                  >
                    <td class="app-table-cell-main">{{ member.label }}</td>
                    <td>{{ member.role }}</td>
                    <td>{{ member.statusLabel }}</td>
                    <td>{{ member.joinedAtLabel }}</td>
                    <td v-if="!isViewOnlyMode" class="app-table-cell-right">
                      <div class="inline-flex items-center justify-end gap-1">
                        <button
                          type="button"
                          :title="'Zum Verwalter machen'"
                          aria-label="Zum Verwalter machen"
                          :disabled="isEditSubmitting || isDeletingGroup || isRemovingMember || isUpdatingMemberRole || !canPromoteMember(member)"
                          class="inline-flex h-8 w-8 items-center justify-center rounded-md text-gray-600 transition hover:bg-gray-100 hover:text-blue-700 disabled:cursor-not-allowed disabled:opacity-40"
                          @click="promoteEditMember(index)"
                        >
                          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" class="h-4 w-4">
                            <path d="M10 2a4 4 0 0 0-4 4v1.086A3.5 3.5 0 0 0 3.5 10.5v4A3.5 3.5 0 0 0 7 18h6a3.5 3.5 0 0 0 3.5-3.5v-4A3.5 3.5 0 0 0 14 7.086V6a4 4 0 0 0-4-4Zm-2.5 5V6a2.5 2.5 0 0 1 5 0v1H7.5ZM13 9a1 1 0 0 1 .95.684l.113.346h.364a1 1 0 0 1 .588 1.809l-.294.214.112.347a1 1 0 0 1-1.538 1.118L13 13.303l-.295.215a1 1 0 0 1-1.538-1.118l.113-.347-.295-.214a1 1 0 0 1 .589-1.809h.364l.113-.346A1 1 0 0 1 13 9Z" />
                          </svg>
                        </button>
                        <button
                          type="button"
                          :title="'Verwalterrechte entziehen'"
                          aria-label="Verwalterrechte entziehen"
                          :disabled="isEditSubmitting || isDeletingGroup || isRemovingMember || isUpdatingMemberRole || !canDemoteMember(member)"
                          class="inline-flex h-8 w-8 items-center justify-center rounded-md text-gray-600 transition hover:bg-gray-100 hover:text-amber-700 disabled:cursor-not-allowed disabled:opacity-40"
                          @click="demoteEditMember(index)"
                        >
                          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" class="h-4 w-4">
                            <path d="M10 2a4 4 0 0 0-4 4v1.086A3.5 3.5 0 0 0 3.5 10.5v4A3.5 3.5 0 0 0 7 18h6a3.5 3.5 0 0 0 3.5-3.5v-4A3.5 3.5 0 0 0 14 7.086V6a4 4 0 0 0-4-4Zm-2.5 5V6a2.5 2.5 0 0 1 5 0v1H7.5Zm2.5 4a.75.75 0 0 1 .75.75V12h1a.75.75 0 0 1 0 1.5h-3.5a.75.75 0 0 1 0-1.5h1v-.25A.75.75 0 0 1 10 11Z" />
                          </svg>
                        </button>
                        <button
                          type="button"
                          :title="'Mitglied entfernen'"
                          aria-label="Mitglied entfernen"
                          :disabled="isEditSubmitting || isDeletingGroup || isRemovingMember || isUpdatingMemberRole"
                          class="inline-flex h-8 w-8 items-center justify-center rounded-md text-red-600 transition hover:bg-red-50 hover:text-red-700 disabled:cursor-not-allowed disabled:opacity-40"
                          @click="removeEditMember(index)"
                        >
                          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" class="h-4 w-4">
                            <path fill-rule="evenodd" d="M8.75 2.5a1.5 1.5 0 0 0-1.5 1.5V5H5.5a.75.75 0 0 0 0 1.5h.286l.603 8.243A2 2 0 0 0 8.384 16.6h3.232a2 2 0 0 0 1.995-1.857l.603-8.243h.286a.75.75 0 0 0 0-1.5h-1.75V4a1.5 1.5 0 0 0-1.5-1.5h-2.5Zm2.5 2.5V4h-2.5v1h2.5Zm-2 3a.75.75 0 0 0-1.5 0v5a.75.75 0 0 0 1.5 0V8Zm3.5-.75a.75.75 0 0 1 .75.75v5a.75.75 0 0 1-1.5 0V8a.75.75 0 0 1 .75-.75Z" clip-rule="evenodd" />
                          </svg>
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="pt-2">
            <h3 class="app-subsection-title mb-3">Anstehende Veranstaltungen</h3>
            <div class="overflow-x-auto">
              <table class="app-table">
                <thead>
                  <tr>
                    <th>Titel</th>
                    <th>Datum/Uhrzeit</th>
                    <th>Ort</th>
                    <th>Beschreibung</th>
                    <th class="app-table-col-right">Mein Status</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="isLoadingGroupActivities">
                    <td colspan="5" class="app-table-cell-muted">Veranstaltungen werden geladen...</td>
                  </tr>
                  <tr v-else-if="groupActivitiesError">
                    <td colspan="5" class="app-table-cell-error">{{ groupActivitiesError }}</td>
                  </tr>
                  <tr v-else-if="groupActivities.length === 0">
                    <td colspan="5" class="app-table-cell-muted">Keine anstehenden Veranstaltungen.</td>
                  </tr>
                  <tr
                    v-for="activity in groupActivities"
                    v-else
                    :key="activity.id"
                  >
                    <td class="app-table-cell-main">{{ activity.title }}</td>
                    <td>{{ activity.startLabel }}</td>
                    <td>{{ activity.location }}</td>
                    <td>{{ activity.description }}</td>
                    <td class="app-table-cell-right">
                      <select
                        :value="activity.status"
                        :disabled="Boolean(updatingActivityStatus[activity.id])"
                        class="rounded-md border border-gray-300 px-2 py-1 text-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
                        @change="updateParticipationStatus(activity.id, String(($event.target as HTMLSelectElement).value))"
                      >
                        <option value="PENDING">{{ participationStatusLabel('PENDING') }}</option>
                        <option value="ACCEPTED">{{ participationStatusLabel('ACCEPTED') }}</option>
                        <option value="DECLINED">{{ participationStatusLabel('DECLINED') }}</option>
                      </select>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div
          v-if="!isViewOnlyMode"
          class="mt-8 pt-5 border-t border-gray-200 flex flex-col gap-3 md:flex-row md:items-center md:justify-between"
        >
          <button
            type="button"
            :disabled="isEditSubmitting || isLoadingEditData || isDeletingGroup || isRemovingMember || isUpdatingMemberRole"
            class="app-btn-danger w-full md:w-auto"
            :class="{ 'cursor-not-allowed opacity-70': isEditSubmitting || isLoadingEditData || isDeletingGroup || isRemovingMember || isUpdatingMemberRole }"
            @click="deleteGroup"
          >
            {{ isDeletingGroup ? 'Gruppe wird gelöscht...' : 'Gruppe löschen' }}
          </button>
          <button
            type="submit"
            :disabled="isEditSubmitting || isLoadingEditData || isDeletingGroup || isRemovingMember || isUpdatingMemberRole"
            class="app-btn-success w-full md:w-auto"
            :class="{ 'cursor-not-allowed opacity-70': isEditSubmitting || isLoadingEditData || isDeletingGroup || isRemovingMember || isUpdatingMemberRole }"
          >
            {{ isEditSubmitting ? 'Gruppe wird gespeichert...' : 'Änderungen speichern' }}
          </button>
        </div>
      </form>
    </div>

    <div
      v-if="showJoinByTokenForm"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      @click.self="closeJoinByTokenForm"
    >
      <form
        class="w-full max-w-lg bg-white rounded-lg shadow-xl p-6 md:p-8 fade-in"
        @submit.prevent="joinGroupByToken"
      >
        <div class="flex items-start justify-between mb-6">
          <h2 class="app-card-title">Mit Einladungstoken beitreten</h2>
          <button
            type="button"
            class="text-gray-500 hover:text-gray-700 text-xl leading-none"
            aria-label="Popup schliessen"
            :disabled="isJoiningByToken"
            @click="closeJoinByTokenForm"
          >
            x
          </button>
        </div>

        <p v-if="joinByTokenErrorMessage" class="app-alert-error mb-4">
          {{ joinByTokenErrorMessage }}
        </p>

        <div class="space-y-5">
          <div>
            <label for="joinTokenInput" class="block text-sm font-medium text-gray-700 mb-1">
              Einladungstoken <span class="text-red-600">*</span>
            </label>
            <input
              id="joinTokenInput"
              v-model.trim="joinByTokenInput"
              type="text"
              required
              :disabled="isJoiningByToken"
              placeholder="Token eingeben"
              class="w-full rounded-lg border border-gray-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
        </div>

        <div class="mt-8 pt-5 border-t border-gray-200 flex flex-col gap-3 sm:flex-row sm:justify-end">
          <button
            type="button"
            :disabled="isJoiningByToken"
            class="app-btn-secondary w-full sm:w-auto"
            :class="{ 'cursor-not-allowed opacity-70': isJoiningByToken }"
            @click="closeJoinByTokenForm"
          >
            Abbrechen
          </button>
          <button
            type="submit"
            :disabled="isJoiningByToken"
            class="app-btn-success w-full sm:w-auto"
            :class="{ 'cursor-not-allowed opacity-70': isJoiningByToken }"
          >
            {{ isJoiningByToken ? 'Beitritt läuft...' : 'Beitreten' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import axios from 'axios'
import { onMounted, reactive, ref } from 'vue'
import { activityService } from '../api/activityService'
import { authService } from '../api/authService'
import { groupService, type CreateGroupRequest, type Group, type GroupActivitySummary, type UpdateGroupRequest } from '../api/groupService'

const showCreateForm = ref(false)
const memberInput = ref('')
const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const groups = ref<Group[]>([])
const isLoadingGroups = ref(false)
const groupsError = ref('')
const showEditForm = ref(false)
const showJoinByTokenForm = ref(false)
const editMemberInput = ref('')
const isEditSubmitting = ref(false)
const isLoadingEditData = ref(false)
const editErrorMessage = ref('')
const memberErrorMessage = ref('')
const editMemberErrorMessage = ref('')
const isInvitingMember = ref(false)
const isRemovingMember = ref(false)
const isUpdatingMemberRole = ref(false)
const initialEditMembers = ref<GroupMemberRow[]>([])
const pendingInviteEmails = ref<string[]>([])
const editingGroupId = ref<number | null>(null)
const isDeletingGroup = ref(false)
const isViewOnlyMode = ref(false)
const currentUserId = ref<number | null>(null)
const editableGroups = ref<Record<number, boolean>>({})
const groupActivities = ref<GroupActivityRow[]>([])
const isLoadingGroupActivities = ref(false)
const groupActivitiesError = ref('')
const updatingActivityStatus = ref<Record<number, boolean>>({})
const joinByTokenInput = ref('')
const joinByTokenErrorMessage = ref('')
const isJoiningByToken = ref(false)

interface GroupMemberRow {
  userId: number | null
  label: string
  email: string
  role: string
  statusLabel: string
  joinedAtLabel: string
}

type ParticipationStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED'

interface GroupActivityRow {
  id: number
  title: string
  description: string
  location: string
  startTime: string
  startLabel: string
  status: ParticipationStatus
}

const form = reactive({
  groupName: '',
  description: '',
  members: [] as string[],
})

const editForm = reactive({
  groupName: '',
  description: '',
  members: [] as GroupMemberRow[],
})

const cloneMemberRows = (members: GroupMemberRow[]): GroupMemberRow[] => members.map((member) => ({ ...member }))

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const normalizeEmail = (value: string): string => value.trim().toLowerCase()

const isValidEmail = (value: string): boolean => EMAIL_REGEX.test(normalizeEmail(value))

const resolveGroupId = (group: unknown): number | null => {
  if (!group || typeof group !== 'object') return null
  const record = group as Record<string, unknown>
  const candidate = record.id ?? record.groupId
  const parsed = Number(candidate)
  return Number.isFinite(parsed) ? parsed : null
}

const formatDateLabel = (value: unknown): string => {
  if (typeof value !== 'string' || !value.trim()) return '-'
  const parsedDate = new Date(value)
  if (Number.isNaN(parsedDate.getTime())) return '-'

  return parsedDate.toLocaleDateString('de-DE', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

const formatDateTimeLabel = (value: unknown): string => {
  if (typeof value !== 'string' || !value.trim()) return '-'
  const parsedDate = new Date(value)
  if (Number.isNaN(parsedDate.getTime())) return '-'

  return parsedDate.toLocaleString('de-DE', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const parseDate = (value: unknown): Date | null => {
  if (typeof value !== 'string' || !value.trim()) return null
  const parsedDate = new Date(value)
  return Number.isNaN(parsedDate.getTime()) ? null : parsedDate
}

const isUpcomingFromToday = (value: unknown): boolean => {
  const parsed = parseDate(value)
  if (!parsed) return false
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return parsed >= today
}

const asString = (value: unknown): string => (typeof value === 'string' ? value.trim() : '')

const formatMemberStatus = (value: unknown): string => {
  const status = asString(value).toUpperCase()
  if (status === 'ACTIVE') return 'Aktiv'
  if (status === 'PENDING') return 'Warten auf Rueckmeldung'
  return status ? `${status}` : '-'
}

const normalizeMemberRole = (value: unknown): 'VERWALTER' | 'MITGLIED' | '' => {
  const role = asString(value).toUpperCase()
  if (role === 'VERWALTER') return 'VERWALTER'
  if (role === 'MITGLIED') return 'MITGLIED'
  return ''
}

const canPromoteMember = (member: GroupMemberRow): boolean => {
  return member.userId !== null && normalizeMemberRole(member.role) === 'MITGLIED'
}

const canDemoteMember = (member: GroupMemberRow): boolean => {
  return member.userId !== null && normalizeMemberRole(member.role) === 'VERWALTER'
}

const loadCurrentUserId = async (): Promise<void> => {
  if (currentUserId.value !== null) return
  try {
    const profile = await authService.getMyProfile()
    const parsedId = Number((profile as { id?: unknown }).id)
    currentUserId.value = Number.isFinite(parsedId) ? parsedId : null
  } catch {
    currentUserId.value = null
  }
}

const canEditGroup = (groupId: number): boolean => Boolean(editableGroups.value[groupId])

const loadEditPermissions = async (nextGroups: Group[]) => {
  editableGroups.value = {}
  if (nextGroups.length === 0) return

  await loadCurrentUserId()
  if (currentUserId.value === null) return

  const permissions = await Promise.all(
    nextGroups.map(async (group) => {
      try {
        const members = await groupService.getGroupMembers(group.id)
        const canEdit = members.some((member) => {
          const memberUserId = Number(member.userId)
          const memberRole = asString(member.role).toUpperCase()
          const memberStatus = asString(member.status).toUpperCase()
          return Number.isFinite(memberUserId) && memberUserId === currentUserId.value && memberRole === 'VERWALTER' && memberStatus === 'ACTIVE'
        })
        return [group.id, canEdit] as const
      } catch {
        return [group.id, false] as const
      }
    })
  )

  editableGroups.value = Object.fromEntries(permissions)
}

const resetForm = () => {
  form.groupName = ''
  form.description = ''
  form.members = []
  memberInput.value = ''
  memberErrorMessage.value = ''
}

const resetEditForm = () => {
  editForm.groupName = ''
  editForm.description = ''
  editForm.members = []
  initialEditMembers.value = []
  pendingInviteEmails.value = []
  groupActivities.value = []
  groupActivitiesError.value = ''
  updatingActivityStatus.value = {}
  editMemberInput.value = ''
  editMemberErrorMessage.value = ''
  editingGroupId.value = null
  isViewOnlyMode.value = false
}

const openCreateForm = () => {
  resetForm()
  errorMessage.value = ''
  successMessage.value = ''
  showCreateForm.value = true
}

const closeCreateForm = () => {
  if (isSubmitting.value) return
  errorMessage.value = ''
  showCreateForm.value = false
}

const closeEditForm = () => {
  if (isEditSubmitting.value || isLoadingEditData.value || isDeletingGroup.value || isRemovingMember.value || isUpdatingMemberRole.value) return
  editErrorMessage.value = ''
  showEditForm.value = false
  resetEditForm()
}

const openJoinByTokenForm = () => {
  if (isJoiningByToken.value) return
  joinByTokenInput.value = ''
  joinByTokenErrorMessage.value = ''
  successMessage.value = ''
  showJoinByTokenForm.value = true
}

const closeJoinByTokenForm = () => {
  if (isJoiningByToken.value) return
  joinByTokenErrorMessage.value = ''
  showJoinByTokenForm.value = false
}

const addMember = () => {
  const value = memberInput.value.trim()
  if (!value) return
  const normalizedValue = normalizeEmail(value)

  if (!isValidEmail(normalizedValue)) {
    memberErrorMessage.value = 'Bitte gib eine gültige E-Mail-Adresse ein.'
    return
  }

  const alreadyAdded = form.members.some((member) => normalizeEmail(member) === normalizedValue)
  if (alreadyAdded) {
    memberErrorMessage.value = 'Diese E-Mail-Adresse wurde bereits hinzugefügt.'
    return
  }

  form.members.push(normalizedValue)
  memberInput.value = ''
  memberErrorMessage.value = ''
}

const removeMember = (index: number) => {
  form.members.splice(index, 1)
}

const addEditMember = () => {
  if (isViewOnlyMode.value) return
  if (isEditSubmitting.value || isDeletingGroup.value || isLoadingEditData.value) return

  const value = editMemberInput.value.trim()
  if (!value) return
  const normalizedValue = normalizeEmail(value)
  const groupId = editingGroupId.value
  if (!groupId) return

  if (!isValidEmail(normalizedValue)) {
    editMemberErrorMessage.value = 'Bitte gib eine gueltige E-Mail-Adresse ein.'
    return
  }

  const alreadyAdded = editForm.members.some((member) => normalizeEmail(member.email) === normalizedValue)
  if (alreadyAdded) {
    editMemberErrorMessage.value = 'Diese E-Mail-Adresse ist bereits Mitglied oder eingeladen.'
    return
  }

  editMemberErrorMessage.value = ''
  editForm.members.push({
    userId: null,
    label: normalizedValue,
    email: normalizedValue,
    role: 'MITGLIED',
    statusLabel: 'Warten auf Rueckmeldung',
    joinedAtLabel: '-'
  })

  if (!pendingInviteEmails.value.includes(normalizedValue)) {
    pendingInviteEmails.value.push(normalizedValue)
  }

  editMemberInput.value = ''
}

const updateEditMemberRole = (index: number, nextRole: 'VERWALTER' | 'MITGLIED') => {
  if (isViewOnlyMode.value) return
  if (isEditSubmitting.value || isDeletingGroup.value || isLoadingEditData.value) return

  const member = editForm.members[index]
  if (!member || member.userId === null) return

  const currentRole = normalizeMemberRole(member.role)
  if (!currentRole || currentRole === nextRole) return

  member.role = nextRole
}

const promoteEditMember = (index: number) => {
  updateEditMemberRole(index, 'VERWALTER')
}

const demoteEditMember = (index: number) => {
  updateEditMemberRole(index, 'MITGLIED')
}

const removeEditMember = (index: number) => {
  if (isViewOnlyMode.value) return
  if (isEditSubmitting.value || isDeletingGroup.value || isLoadingEditData.value) return

  const member = editForm.members[index]
  if (!member) return

  if (member.userId === null) {
    const memberEmail = normalizeEmail(member.email)
    pendingInviteEmails.value = pendingInviteEmails.value.filter((email) => email !== memberEmail)
    editForm.members.splice(index, 1)
    return
  }

  const confirmed = window.confirm(`Mitglied ${member.label} wirklich entfernen?`)
  if (!confirmed) return

  editMemberErrorMessage.value = ''
  editForm.members.splice(index, 1)
}

const extractMembers = (membersPayload: unknown): GroupMemberRow[] => {
  if (!Array.isArray(membersPayload)) return []

  return membersPayload
    .map((member) => {
      if (typeof member === 'string') {
        const email = asString(member)
        return {
          userId: null,
          label: email || '-',
          email,
          role: 'Mitglied',
          statusLabel: '-',
          joinedAtLabel: '-'
        }
      }
      if (!member || typeof member !== 'object') return null

      const data = member as Record<string, unknown>
      const displayName =
        asString(data.displayName) ||
        asString(data.username) ||
        asString(data.name) ||
        asString(data.fullName)
      const email = asString(data.email) || asString(data.userEmail)
      const role =
        asString(data.role) ||
        asString(data.memberRole) ||
        asString(data.groupRole) ||
        'MITGLIED'
      const status = data.status ?? data.memberStatus ?? data.invitationStatus
      const joinedAtRaw = data.joinedAt ?? data.createdAt ?? data.memberSince ?? data.joinDate

      return {
        userId: Number.isFinite(Number(data.userId)) ? Number(data.userId) : null,
        label: displayName || email || '-',
        email,
        role,
        statusLabel: formatMemberStatus(status),
        joinedAtLabel: formatDateLabel(joinedAtRaw)
      }
    })
    .filter((member): member is GroupMemberRow => Boolean(member))
}

const normalizeParticipationStatus = (value: unknown): ParticipationStatus => {
  const status = asString(value).toUpperCase()
  if (status === 'ACCEPTED') return 'ACCEPTED'
  if (status === 'DECLINED') return 'DECLINED'
  return 'PENDING'
}

const participationStatusLabel = (status: ParticipationStatus): string => {
  if (status === 'ACCEPTED') return 'Zugesagt'
  if (status === 'DECLINED') return 'Abgesagt'
  return 'Auf Vorbehalt'
}

const loadGroupActivities = async (groupId: number) => {
  isLoadingGroupActivities.value = true
  groupActivitiesError.value = ''
  groupActivities.value = []

  try {
    const activities = await groupService.getGroupActivities(groupId)
    const upcomingActivities = activities
      .filter((activity) => isUpcomingFromToday((activity as GroupActivitySummary).startTime))
      .sort((a, b) => {
        const startA = parseDate((a as GroupActivitySummary).startTime)?.getTime() ?? 0
        const startB = parseDate((b as GroupActivitySummary).startTime)?.getTime() ?? 0
        return startA - startB
      })

    const mapped = await Promise.all(
      upcomingActivities.map(async (activity) => {
        const activityId = Number(activity.id)
        let status: ParticipationStatus = 'PENDING'

        if (Number.isFinite(activityId) && currentUserId.value !== null) {
          try {
            const participants = await activityService.getParticipants(activityId)
            const ownParticipant = participants.find((participant) => Number(participant.userId) === currentUserId.value)
            status = normalizeParticipationStatus(ownParticipant?.status)
          } catch {
            status = 'PENDING'
          }
        }

        return {
          id: activityId,
          title: asString(activity.title) || 'Ohne Titel',
          description: asString(activity.description) || '-',
          location: asString(activity.location) || '-',
          startTime: asString(activity.startTime),
          startLabel: formatDateTimeLabel(activity.startTime),
          status
        } as GroupActivityRow
      })
    )

    groupActivities.value = mapped.filter((activity) => Number.isFinite(activity.id))
  } catch (error) {
    groupActivitiesError.value = 'Anstehende Veranstaltungen konnten nicht geladen werden.'
    if (axios.isAxiosError(error)) {
      const backendMessage = (error.response?.data as { message?: string } | undefined)?.message
      if (backendMessage) groupActivitiesError.value = backendMessage
    }
  } finally {
    isLoadingGroupActivities.value = false
  }
}

const updateParticipationStatus = async (activityId: number, rawStatus: string) => {
  const nextStatus = normalizeParticipationStatus(rawStatus)
  if (!Number.isFinite(activityId)) return
  if (updatingActivityStatus.value[activityId]) return

  const current = groupActivities.value.find((activity) => activity.id === activityId)
  if (!current) return

  const previousStatus = current.status
  current.status = nextStatus

  if (nextStatus === 'PENDING' && previousStatus !== 'PENDING') {
    current.status = previousStatus
    editErrorMessage.value = 'Auf Vorbehalt kann nur gesetzt werden, solange noch keine Zu-/Absage erfolgt ist.'
    return
  }

  updatingActivityStatus.value = {
    ...updatingActivityStatus.value,
    [activityId]: true
  }

  try {
    await activityService.respondToActivity(activityId, nextStatus)
    successMessage.value = 'Dein Veranstaltungsstatus wurde aktualisiert.'
  } catch (error) {
    current.status = previousStatus
    if (axios.isAxiosError(error)) {
      const backendMessage = (error.response?.data as { message?: string } | undefined)?.message
      editErrorMessage.value = backendMessage || 'Status konnte nicht aktualisiert werden.'
    } else {
      editErrorMessage.value = 'Status konnte nicht aktualisiert werden.'
    }
  } finally {
    const nextMap = { ...updatingActivityStatus.value }
    delete nextMap[activityId]
    updatingActivityStatus.value = nextMap
  }
}

const applyEditMemberChanges = async (groupId: number): Promise<{ invited: number; removed: number; roleUpdated: number }> => {
  const initialByUserId = new Map<number, GroupMemberRow>()
  for (const member of initialEditMembers.value) {
    if (member.userId !== null) initialByUserId.set(member.userId, member)
  }

  const currentByUserId = new Map<number, GroupMemberRow>()
  for (const member of editForm.members) {
    if (member.userId !== null) currentByUserId.set(member.userId, member)
  }

  let removed = 0
  for (const [userId] of initialByUserId) {
    if (!currentByUserId.has(userId)) {
      await groupService.removeGroupMember(groupId, userId)
      removed += 1
    }
  }

  let roleUpdated = 0
  for (const [userId, currentMember] of currentByUserId) {
    const initialMember = initialByUserId.get(userId)
    if (!initialMember) continue
    const initialRole = normalizeMemberRole(initialMember.role)
    const currentRole = normalizeMemberRole(currentMember.role)
    if (!currentRole || initialRole === currentRole) continue
    await groupService.updateGroupMemberRole(groupId, userId, { role: currentRole })
    roleUpdated += 1
  }

  let invited = 0
  for (const email of pendingInviteEmails.value) {
    const isStillPending = editForm.members.some(
      (member) => member.userId === null && normalizeEmail(member.email) === email
    )
    if (!isStillPending) continue
    await groupService.inviteMember(groupId, { email })
    invited += 1
  }

  return { invited, removed, roleUpdated }
}

const openGroupForm = async (group: Group, viewOnly: boolean) => {
  if (isSubmitting.value || isEditSubmitting.value || isDeletingGroup.value || isInvitingMember.value || isRemovingMember.value || isUpdatingMemberRole.value) return
  if (!viewOnly && !canEditGroup(group.id)) return

  resetEditForm()
  isViewOnlyMode.value = viewOnly
  editErrorMessage.value = ''
  successMessage.value = ''
  showEditForm.value = true
  isLoadingEditData.value = true

  try {
    await loadCurrentUserId()
    const [groupDetails, groupMembers] = await Promise.all([
      groupService.getGroup(group.id),
      groupService.getGroupMembers(group.id)
    ])

    editingGroupId.value = group.id
    editForm.groupName = (groupDetails.name || group.name || '').trim()
    editForm.description = (groupDetails.description || group.description || '').trim()
    editForm.members = extractMembers(groupMembers)
    initialEditMembers.value = cloneMemberRows(editForm.members)
    pendingInviteEmails.value = []
    await loadGroupActivities(group.id)
  } catch (error) {
    showEditForm.value = false
    resetEditForm()

    if (axios.isAxiosError(error)) {
      const backendMessage = (error.response?.data as { message?: string } | undefined)?.message
      groupsError.value = backendMessage || 'Gruppendetails konnten nicht geladen werden.'
    } else {
      groupsError.value = 'Gruppendetails konnten nicht geladen werden.'
    }
  } finally {
    isLoadingEditData.value = false
  }
}

const openViewForm = async (group: Group) => {
  await openGroupForm(group, true)
}

const openEditForm = async (group: Group) => {
  await openGroupForm(group, false)
}

const loadGroups = async () => {
  isLoadingGroups.value = true
  groupsError.value = ''

  try {
    const nextGroups = await groupService.getMyGroups()
    groups.value = nextGroups
    await loadEditPermissions(nextGroups)
  } catch {
    groupsError.value = 'Gruppen konnten nicht geladen werden.'
  } finally {
    isLoadingGroups.value = false
  }
}

const createGroup = async () => {
  const name = form.groupName.trim()
  if (!name || isSubmitting.value) return

  isSubmitting.value = true
  errorMessage.value = ''
  memberErrorMessage.value = ''

  try {
    const payload: CreateGroupRequest = { name }
    const description = form.description.trim()
    if (description) payload.description = description
    const createdGroup = await groupService.createGroup(payload)
    const groupId = resolveGroupId(createdGroup)

    if (groupId && form.members.length) {
      const inviteResults = await Promise.allSettled(
        form.members.map((email) => groupService.inviteMember(groupId, { email: normalizeEmail(email) }))
      )
      const failedInvites = inviteResults.filter((result) => result.status === 'rejected').length
      if (failedInvites > 0) {
        successMessage.value = `Gruppe angelegt, aber ${failedInvites} Einladung(en) konnten nicht versendet werden.`
      } else {
        successMessage.value = 'Gruppe wurde erfolgreich angelegt und Einladungen wurden versendet.'
      }
    } else if (!groupId && form.members.length) {
      successMessage.value = 'Gruppe wurde angelegt. Einladungen konnten nicht automatisch versendet werden.'
    } else {
      successMessage.value = 'Gruppe wurde erfolgreich angelegt.'
    }

    await loadGroups()

    showCreateForm.value = false
    resetForm()
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const backendMessage = (error.response?.data as { message?: string } | undefined)?.message
      errorMessage.value = backendMessage || 'Gruppe konnte nicht angelegt werden.'
    } else {
      errorMessage.value = 'Gruppe konnte nicht angelegt werden.'
    }
  } finally {
    isSubmitting.value = false
  }
}

const joinGroupByToken = async () => {
  const token = joinByTokenInput.value.trim()
  if (!token || isJoiningByToken.value) return

  isJoiningByToken.value = true
  joinByTokenErrorMessage.value = ''

  try {
    await groupService.joinByInviteToken(token)
    await loadGroups()
    successMessage.value = 'Du bist der Gruppe erfolgreich beigetreten.'
    showJoinByTokenForm.value = false
    joinByTokenInput.value = ''
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const backendMessage = (error.response?.data as { message?: string } | undefined)?.message
      joinByTokenErrorMessage.value = backendMessage || 'Beitritt mit Einladungstoken nicht möglich.'
    } else {
      joinByTokenErrorMessage.value = 'Beitritt mit Einladungstoken nicht möglich.'
    }
  } finally {
    isJoiningByToken.value = false
  }
}

const updateGroup = async () => {
  if (isViewOnlyMode.value) return
  const groupId = editingGroupId.value
  const name = editForm.groupName.trim()
  if (!groupId || !name || isEditSubmitting.value || isLoadingEditData.value || isInvitingMember.value || isRemovingMember.value || isUpdatingMemberRole.value) return

  isEditSubmitting.value = true
  editErrorMessage.value = ''

  try {
    const payload: UpdateGroupRequest = { name }
    const description = editForm.description.trim()
    if (description) payload.description = description

    await groupService.updateGroup(groupId, payload)
    const changeSummary = await applyEditMemberChanges(groupId)
    await loadGroups()

    const summaryParts: string[] = []
    if (changeSummary.roleUpdated > 0) summaryParts.push(`${changeSummary.roleUpdated} Rolle(n) angepasst`)
    if (changeSummary.removed > 0) summaryParts.push(`${changeSummary.removed} Mitglied(er) entfernt`)
    if (changeSummary.invited > 0) summaryParts.push(`${changeSummary.invited} Einladung(en) versendet`)
    successMessage.value =
      summaryParts.length > 0
        ? `Gruppe wurde erfolgreich aktualisiert (${summaryParts.join(', ')}).`
        : 'Gruppe wurde erfolgreich aktualisiert.'
    showEditForm.value = false
    resetEditForm()
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const backendMessage = (error.response?.data as { message?: string } | undefined)?.message
      editErrorMessage.value = backendMessage || 'Gruppe konnte nicht aktualisiert werden.'
    } else {
      editErrorMessage.value = 'Gruppe konnte nicht aktualisiert werden.'
    }
  } finally {
    isEditSubmitting.value = false
  }
}

const deleteGroup = async () => {
  if (isViewOnlyMode.value) return
  const groupId = editingGroupId.value
  if (!groupId || isDeletingGroup.value || isEditSubmitting.value || isLoadingEditData.value || isRemovingMember.value || isUpdatingMemberRole.value) return

  const confirmed = window.confirm('Möchtest du diese Gruppe wirklich löschen?')
  if (!confirmed) return

  isDeletingGroup.value = true
  editErrorMessage.value = ''

  try {
    await groupService.deleteGroup(groupId)
    await loadGroups()

    successMessage.value = 'Gruppe wurde erfolgreich gelöscht.'
    showEditForm.value = false
    resetEditForm()
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const backendMessage = (error.response?.data as { message?: string } | undefined)?.message
      editErrorMessage.value = backendMessage || 'Gruppe konnte nicht gelöscht werden.'
    } else {
      editErrorMessage.value = 'Gruppe konnte nicht gelöscht werden.'
    }
  } finally {
    isDeletingGroup.value = false
  }
}

onMounted(() => {
  void loadGroups()
})
</script>
