// src/features/itemType/pages/ItemTypeTablePage.tsx

import { useRef, useState } from 'react';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Toolbar } from 'primereact/toolbar';
import { Toast } from 'primereact/toast';

import { ItemType } from '../types/ItemType';
import { ItemTypeCreateDialog } from '../components/ItemTypeCreateDialog';
import { ItemTypeEditDialog } from '../components/ItemTypeEditDialog';
import { ItemTypeDeleteDialog } from '../components/ItemTypeDeleteDialog';

import {
  useItemTypes,
  useCreateItemType,
  useUpdateItemType,
  useDeleteItemType,
} from '../hooks/useItemType';

export function ItemTypeTablePage() {
  const toast = useRef<Toast>(null);

  const [createVisible, setCreateVisible] = useState(false);
  const [editVisible, setEditVisible] = useState(false);
  const [deleteVisible, setDeleteVisible] = useState(false);

  const [selected, setSelected] = useState<ItemType | null>(null);

  // Queries
  const itemTypesQuery = useItemTypes();

  // Mutations
  const createMutation = useCreateItemType();
  const updateMutation = useUpdateItemType();
  const deleteMutation = useDeleteItemType();

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
  const handleCreate = async (value: ItemType) => {
    if (!value.id || value.id <= 0) {
      showError('ID must be a positive integer greater than 0');
      return;
    }

    try {
      await createMutation.mutateAsync(value);
      showSuccess('Item Type created');
    } catch {
      showError('Failed to create Item Type');
    }
  };

  // UPDATE
  const handleUpdate = async (value: ItemType) => {
    if (!value.name.trim()) {
      showError('Name is required');
      return;
    }

    try {
      await updateMutation.mutateAsync(value);
      showSuccess('Item Type updated');
    } catch {
      showError('Failed to update Item Type');
    }
  };

  // DELETE
  const handleDelete = async () => {
    if (!selected || !selected.id || selected.id <= 0) {
      showError('Invalid ID');
      return;
    }

    try {
      await deleteMutation.mutateAsync(selected.id);
      showSuccess('Item Type deleted');
    } catch {
      showError('Failed to delete Item Type');
    }
  };

  // Row actions
  const actionTemplate = (row: ItemType) => (
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
    <Button label="Refresh" icon="pi pi-refresh" onClick={() => itemTypesQuery.refetch()} />
  );

  return (
    <div className="p-4">
      <Toast ref={toast} />

      <Toolbar left={leftToolbar} right={rightToolbar} className="mb-3" />

      <DataTable
        value={itemTypesQuery.data?.data ?? []} // unwrap ApiResponse<T>
        loading={itemTypesQuery.isLoading}
        paginator
        rows={10}
        tableStyle={{ minWidth: '40rem' }}
      >
        <Column field="id" header="ID" sortable />
        <Column field="name" header="Name" sortable />
        <Column header="Actions" body={actionTemplate} />
      </DataTable>

      {/* CREATE */}
      <ItemTypeCreateDialog
        visible={createVisible}
        onHide={() => setCreateVisible(false)}
        onCreate={handleCreate}
      />

      {/* EDIT */}
      <ItemTypeEditDialog
        visible={editVisible}
        onHide={() => setEditVisible(false)}
        itemType={selected}
        onUpdate={handleUpdate}
      />

      {/* DELETE */}
      <ItemTypeDeleteDialog
        visible={deleteVisible}
        onHide={() => setDeleteVisible(false)}
        onConfirm={handleDelete}
      />
    </div>
  );
}
