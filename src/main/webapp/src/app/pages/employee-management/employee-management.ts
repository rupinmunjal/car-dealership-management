import { ChangeDetectorRef, Component, OnInit, ViewChild, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { AppAlert, AppAvatar, AppButton, DataTable, PageHeader } from '../../components';
import { getApiErrorMessage } from '../../utils/api-error';

@Component({
  selector: 'app-employee-management',
  imports: [CommonModule, FormsModule,
    MatIconModule, MatDialogModule,
    MatFormFieldModule, MatInputModule, MatSlideToggleModule,
    AppAlert, AppAvatar, AppButton, DataTable, PageHeader],
  templateUrl: './employee-management.html',
})
export class EmployeeManagement implements OnInit {
  employees: any[] = [];
  message = ''; error = '';
  createForm = { email: '', password: '' };
  editingEmployee: any = null;
  selectedPermissions: string[] = [];
  availablePermissions = ['CAN_ADD_CAR', 'CAN_EDIT_CAR', 'CAN_DELETE_CAR'];
  dialogRef: MatDialogRef<any> | null = null;
  private dealerId: number | null = null;

  @ViewChild('createDialog') createDialog!: TemplateRef<any>;
  @ViewChild('permissionsDialog') permissionsDialog!: TemplateRef<any>;

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private dialog: MatDialog,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.dealerId = this.auth.getDealerId();
    this.load();
  }

  load() {
    this.http.get<any[]>(`/api/v1/dealers/${this.dealerId}/employees`).subscribe({
      next: d => { this.employees = d; this.cd.detectChanges(); },
      error: e => { this.error = getApiErrorMessage(e, 'Failed to load employees'); this.cd.detectChanges(); }
    });
  }

  openCreateDialog() {
    this.createForm = { email: '', password: '' };
    this.dialogRef = this.dialog.open(this.createDialog);
  }

  createEmployee() {
    this.http.post(`/api/v1/dealers/${this.dealerId}/employees`, this.createForm).subscribe({
      next: () => {
        this.message = `Employee ${this.createForm.email} created!`; this.error = '';
        this.load(); this.dialogRef?.close(); this.cd.detectChanges();
      },
      error: e => { this.error = getApiErrorMessage(e, 'Creation failed'); this.message = ''; this.cd.detectChanges(); }
    });
  }

  openPermissionsDialog(emp: any) {
    this.editingEmployee = emp;
    this.selectedPermissions = [...(emp.permissions || [])];
    this.dialogRef = this.dialog.open(this.permissionsDialog);
  }

  togglePermission(perm: string) {
    this.selectedPermissions = this.selectedPermissions.includes(perm)
      ? this.selectedPermissions.filter(p => p !== perm)
      : [...this.selectedPermissions, perm];
  }

  savePermissions() {
    this.http.put(`/api/v1/dealers/${this.dealerId}/employees/${this.editingEmployee.id}/permissions`, { permissions: this.selectedPermissions }).subscribe({
      next: () => {
        this.message = 'Permissions updated!'; this.error = '';
        this.load(); this.dialogRef?.close(); this.cd.detectChanges();
      },
      error: e => { this.error = getApiErrorMessage(e, 'Update failed'); this.message = ''; this.cd.detectChanges(); }
    });
  }

  deactivateEmployee(emp: any) {
    this.http.delete(`/api/v1/dealers/${this.dealerId}/employees/${emp.id}`).subscribe({
      next: () => { this.message = 'Employee deactivated'; this.load(); this.cd.detectChanges(); },
      error: e => { this.error = getApiErrorMessage(e, 'Deactivation failed'); this.cd.detectChanges(); }
    });
  }

  roleChipClass(role: string): string {
    return role === 'DEALER_ADMIN' ? 'chip chip-brand' : 'chip chip-gray';
  }

  roleLabel(role: string): string {
    return role === 'DEALER_ADMIN' ? 'Admin' : 'Employee';
  }

  statusChipClass(active: boolean): string {
    return active ? 'chip chip-green' : 'chip chip-red';
  }
}
