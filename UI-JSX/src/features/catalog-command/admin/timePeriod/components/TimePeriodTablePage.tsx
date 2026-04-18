// src/features/itemType/pages/ItemTypeTablePage.tsx

import { useRef, useState } from 'react';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Toolbar } from 'primereact/toolbar';
import { Toast } from 'primereact/toast';

import { TimePeriod } from '../types/TimePeriod';
import { TimePeriodCreateDialog } from './TimePeriodCreateDialog';
import { TimePeriodEditDialog } from './TimePeriodEditDialog';
import { TimePeriodDeleteDialog } from './TimePeriodDeleteDialog';

import {
  useTimePeriods,
  useCreateTimePeriod,
  useUpdateTimePeriod,
  useDeleteTimePeriod,
} from '../hooks/useTimePeriod';

export function TimePeriodTablePage() {
  const toast = useRef<Toast>(null);

  const [createVisible, setCreateVisible] = useState(false);
  const [editVisible, setEditVisible] = useState(false);
  const [deleteVisible, setDeleteVisible] = useState(false);

  const [selected, setSelected] = useState<TimePeriod | null>(null);

  // Queries
  const timePeriodsQuery = useTimePeriods();

  // Mutations
  const createMutation = useCreateTimePeriod();
  const updateMutation = useUpdateTimePeriod();
  const deleteMutation = useDeleteTimePeriod();

  const showSuccess = (msg: string) =>
    toast.current?.show({
      severity: 'success',
      summary: 'Success',
      detail: msg,
    });

  const showError = (msg: string) =>
    toast.current?.show({
      severity: 'error',
      summary: 'Error',
      detail: msg,
    });

  // CREATE
  const handleCreate = async (value: TimePeriod) => {
    if (!value.id || value.id <= 0) {
      showError('ID must be a positive integer greater than 0');
      return;
    }

    try {
      await createMutation.mutateAsync(value);
      showSuccess('Time Period created');
    } catch {
      showError('Failed to create Time Period');
    }
  };

  // UPDATE
  const handleUpdate = async (value: TimePeriod) => {
    if (!value.name.trim()) {
      showError('Name is required');
      return;
    }

    try {
      await updateMutation.mutateAsync(value);
      showSuccess('Time Period updated');
    } catch {
      showError('Failed to update Time Period');
    }
  };

  // DELETE
  const handleDelete = async () => {
    if (!selected || !selected.id) {
      showError('Invalid ID');
      return;
    }

    try {
      await deleteMutation.mutateAsync(selected.id);
      showSuccess('Time Period deleted');
    } catch {
      showError('Failed to delete Time Period');
    }
  };

  // Row actions
  const actionTemplate = (row: TimePeriod) => (
    <div className="flex gap-2">
      <Button
        icon="pi pi-pencil"
        rounded
        onClick={() => {
          setSelected(row);
          setEditVisible(true);
        }}
      />
      <Button
        icon="pi pi-trash"
        severity="danger"
        rounded
        onClick={() => {
          setSelected(row);
          setDeleteVisible(true);
        }}
      />
    </div>
  );

  const leftToolbar = (
    <Button label="New" icon="pi pi-plus" onClick={() => setCreateVisible(true)} />
  );

  const rightToolbar = (
    <Button label="Refresh" icon="pi pi-refresh" onClick={() => timePeriodsQuery.refetch()} />
  );

  return (
    <div className="p-4">
      <Toast ref={toast} />

      <Toolbar left={leftToolbar} right={rightToolbar} className="mb-3" />

      <DataTable
        value={timePeriodsQuery.data?.data ?? []} // unwrap ApiResponse<T>
        loading={timePeriodsQuery.isLoading}
        paginator
        rows={10}
        tableStyle={{ minWidth: '40rem' }}
      >
        <Column field="id" header="ID" sortable />
        <Column field="name" header="Name" sortable />
        <Column header="Actions" body={actionTemplate} />
      </DataTable>

      {/* CREATE */}
      <TimePeriodCreateDialog
        visible={createVisible}
        onHide={() => setCreateVisible(false)}
        onCreate={handleCreate}
      />

      {/* EDIT */}
      <TimePeriodEditDialog
        visible={editVisible}
        onHide={() => setEditVisible(false)}
        timePeriod={selected}
        onUpdate={handleUpdate}
      />

      {/* DELETE */}
      <TimePeriodDeleteDialog
        visible={deleteVisible}
        onHide={() => setDeleteVisible(false)}
        onConfirm={handleDelete}
      />
    </div>
  );
}
