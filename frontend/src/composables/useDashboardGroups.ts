import { ref, computed, onMounted } from 'vue'
import { groupService, type Group, type GroupInvitation } from '../api/groupService'

export function useDashboardGroups() {
  const myGroups = ref<Group[]>([])
  const groupInvitations = ref<GroupInvitation[]>([])
  const isLoadingGroups = ref(false)
  const isLoadingInvitations = ref(false)
  const errorGroups = ref<string | null>(null)
  const errorInvitations = ref<string | null>(null)

  const fetchMyGroups = async () => {
    isLoadingGroups.value = true
    errorGroups.value = null
    try {
      myGroups.value = await groupService.getMyGroups()
    } catch (error) {
      errorGroups.value = 'Fehler beim Laden der Gruppen'
      console.error(error)
    } finally {
      isLoadingGroups.value = false
    }
  }

  const fetchGroupInvitations = async () => {
    isLoadingInvitations.value = true
    errorInvitations.value = null
    try {
      groupInvitations.value = await groupService.getGroupInvitations()
    } catch (error) {
      errorInvitations.value = 'Fehler beim Laden der Einladungen'
      console.error(error)
    } finally {
      isLoadingInvitations.value = false
    }
  }

  const acceptInvitation = async (invitationId: number) => {
    try {
      await groupService.acceptInvitation(invitationId)
      // Remove from invitations and refresh groups
      groupInvitations.value = groupInvitations.value.filter(i => i.id !== invitationId)
      await fetchMyGroups()
    } catch (error) {
      errorInvitations.value = 'Fehler beim Akzeptieren der Einladung'
      console.error(error)
    }
  }

  const declineInvitation = async (invitationId: number) => {
    try {
      await groupService.declineInvitation(invitationId)
      groupInvitations.value = groupInvitations.value.filter(i => i.id !== invitationId)
    } catch (error) {
      errorInvitations.value = 'Fehler beim Ablehnen der Einladung'
      console.error(error)
    }
  }

  const hasInvitations = computed(() => groupInvitations.value.length > 0)
  const pendingInvitationsCount = computed(() => 
    groupInvitations.value.filter(i => i.status === 'PENDING').length
  )

  onMounted(() => {
    fetchMyGroups()
    fetchGroupInvitations()
  })

  return {
    myGroups,
    groupInvitations,
    isLoadingGroups,
    isLoadingInvitations,
    errorGroups,
    errorInvitations,
    fetchMyGroups,
    fetchGroupInvitations,
    acceptInvitation,
    declineInvitation,
    hasInvitations,
    pendingInvitationsCount
  }
}
