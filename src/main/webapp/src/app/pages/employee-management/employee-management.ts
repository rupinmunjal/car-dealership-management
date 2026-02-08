import { Component, OnInit, ViewChild, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth';
import { MatTableModule } from '@angular/material/table';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialogModule, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

@Component({
  selector: 'app-employee-management',
  imports: [CommonModule, FormsModule,
    MatTableModule, MatToolbarModule, MatButtonModule,
    MatIconModule, MatChipsModule, MatTooltipModule,
    MatDialogModule, MatFormFieldModule, MatInputModule,
    MatSlideToggleModule],
  templateUrl: './employee-management.html',
  styleUrl: './employee-management.css',
})
export class EmployeeManagement implements OnInit {
  employees: any[] = [];
  columns = ['id', 'email', 'role', 'active', 'actions'];
  message = ''; error = '';
  createForm = { email: '', password: '' };
  editingEmployee: any = null;
  selectedPermissions: string[] = [];
  availablePermissions = ['CAN_ADD_CAR', 'CAN_EDIT_CAR', 'CAN_DELETE_CAR'];
  dialogRef: MatDialogRef<any> | null = null;
  private dealerId: number | null = null;

  @ViewChild('createDialog') createDialog!: TemplateRef<any>;
  @ViewChild('permissionsDialog') permissionsDialog!: TemplateRef<any>;

  constructor(private http: HttpClient, private auth: AuthService, private dialog: MatDialog) {}

  ngOnInit() {
    this.dealerId = this.auth.getDealerId();
    this.load();
  }

  load() {
    this.http.get<any[]>(`/api/v1/dealers/${this.dealerId}/employees`).subscribe({
      next: d => { this.employees = d; },
      error: e => { this.error = e.error?.message || 'Failed to load employees'; }
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
        this.load(); this.dialogRef?.close();
      },
      error: e => { this.error = e.error?.message || 'Creation failed'; this.message = ''; }
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
    this.http.put(`/api/v1/employees/${this.editingEmployee.id}/permissions`, { permissions: this.selectedPermissions }).subscribe({
      next: () => {
        this.message = 'Permissions updated!'; this.error = '';
        this.load(); this.dialogRef?.close();
      },
      error: e => { this.error = e.error?.message || 'Update failed'; this.message = ''; }
    });
  }

  toggleActive(emp: any) {
    const action = emp.active ? 'deactivate' : 'reactivate';
    const endpoint = emp.active
      ? `/api/v1/employees/${emp.id}/${action}`
      : `/api/v1/employees/${emp.id}/reactivate`;
    this.http.put(endpoint, {}).subscribe({
      next: () => { this.message = `Employee ${action}d!`; this.load(); },
      error: e => { this.error = e.error?.message || `${action} failed`; }
    });
  }
}
